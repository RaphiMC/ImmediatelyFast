/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.immediatelyfast.feature.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.raphimc.immediatelyfast.ImmediatelyFast;

public class ByteBufferBuilderPool {

    private static final ReferenceList<Entry> FREE = new ReferenceArrayList<>();
    private static final ReferenceList<Entry> IN_USE = new ReferenceArrayList<>();
    private static final Reference2ObjectMap<ByteBufferBuilder, Entry> BUFFER_BUILDER_MAPPING = new Reference2ObjectOpenHashMap<>();

    private ByteBufferBuilderPool() {
    }

    public static ByteBufferBuilder borrowBufferBuilder() {
        RenderSystem.assertOnRenderThread();
        Entry entry;
        if (FREE.isEmpty()) {
            entry = new Entry(new ByteBufferBuilder(256));
        } else {
            entry = FREE.removeFirst();
            if (entry.bufferBuilder.pointer == 0L) { // If the buffer was closed while in the pool
                BUFFER_BUILDER_MAPPING.remove(entry.bufferBuilder);
                entry = new Entry(new ByteBufferBuilder(256));
            }
        }
        IN_USE.add(entry);
        BUFFER_BUILDER_MAPPING.put(entry.bufferBuilder, entry);
        entry.onBorrow();
        return entry.bufferBuilder;
    }

    public static void returnBufferBuilderSafe(final ByteBufferBuilder bufferBuilder) {
        RenderSystem.assertOnRenderThread();
        final Entry entry = BUFFER_BUILDER_MAPPING.get(bufferBuilder);
        if (!IN_USE.remove(entry)) {
            return;
        }
        entry.onReturn();
        FREE.addFirst(entry);
    }

    public static int getSize() {
        return FREE.size() + IN_USE.size();
    }

    public static void onEndFrame() {
        if (!IN_USE.isEmpty()) {
            // Reclaim all buffer builders that were not returned to the pool this and the last frame
            final boolean leak = IN_USE.removeIf(entry -> {
                if (entry.inUseOverMultipleFrames) {
                    entry.onReturn();
                    FREE.addFirst(entry);
                    return true;
                }
                return false;
            });
            if (leak) {
                ImmediatelyFast.LOGGER.warn("Some BufferBuilders were not returned to the pool. Forcibly reclaiming them to prevent a memory leak.");
            }

            // Mark all as in use over multiple frames
            for (Entry entry : IN_USE) {
                entry.inUseOverMultipleFrames = true;
            }
        }

        FREE.removeIf(entry -> {
            if (entry.shouldBeClosed()) {
                entry.bufferBuilder.close();
                BUFFER_BUILDER_MAPPING.remove(entry.bufferBuilder);
                return true;
            }
            return false;
        });
    }

    private static class Entry {

        private final ByteBufferBuilder bufferBuilder;
        private long lastAccessTime;
        private boolean inUseOverMultipleFrames;

        public Entry(final ByteBufferBuilder bufferBuilder) {
            this.bufferBuilder = bufferBuilder;
            this.lastAccessTime = System.currentTimeMillis();
        }

        public boolean shouldBeClosed() {
            return System.currentTimeMillis() - this.lastAccessTime > 60 * 1000;
        }

        public void onBorrow() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        public void onReturn() {
            this.bufferBuilder.discard();
            this.inUseOverMultipleFrames = false;
        }

    }

}
