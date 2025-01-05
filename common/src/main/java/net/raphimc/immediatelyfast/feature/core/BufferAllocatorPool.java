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
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.minecraft.client.util.BufferAllocator;
import net.raphimc.immediatelyfast.ImmediatelyFast;

public class BufferAllocatorPool {

    private static final ReferenceList<Entry> FREE = new ReferenceArrayList<>();
    private static final ReferenceList<Entry> IN_USE = new ReferenceArrayList<>();
    private static final Reference2ObjectMap<BufferAllocator, Entry> BUFFER_ALLOCATOR_MAPPING = new Reference2ObjectOpenHashMap<>();

    private BufferAllocatorPool() {
    }

    public static BufferAllocator borrowBufferAllocator() {
        RenderSystem.assertOnRenderThread();
        Entry entry;
        if (FREE.isEmpty()) {
            entry = new Entry(new BufferAllocator(256));
        } else {
            entry = FREE.removeFirst();
            if (entry.bufferAllocator.pointer == 0L) { // If the buffer was closed while in the pool
                BUFFER_ALLOCATOR_MAPPING.remove(entry.bufferAllocator);
                entry = new Entry(new BufferAllocator(256));
            }
        }
        IN_USE.add(entry);
        BUFFER_ALLOCATOR_MAPPING.put(entry.bufferAllocator, entry);
        entry.onBorrow();
        return entry.bufferAllocator;
    }

    public static void returnBufferAllocatorSafe(final BufferAllocator bufferAllocator) {
        RenderSystem.assertOnRenderThread();
        final Entry entry = BUFFER_ALLOCATOR_MAPPING.get(bufferAllocator);
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
            // Reclaim all buffers that were not returned to the pool this and the last frame
            final boolean leak = IN_USE.removeIf(entry -> {
                if (entry.inUseOverMultipleFrames) {
                    entry.onReturn();
                    FREE.addFirst(entry);
                    return true;
                }
                return false;
            });
            if (leak) {
                ImmediatelyFast.LOGGER.warn("Some BufferAllocators were not returned to the pool. Forcibly reclaiming them to prevent a memory leak.");
            }

            // Mark all as in use over multiple frames
            for (Entry entry : IN_USE) {
                entry.inUseOverMultipleFrames = true;
            }
        }

        FREE.removeIf(entry -> {
            if (entry.shouldBeClosed()) {
                entry.bufferAllocator.close();
                BUFFER_ALLOCATOR_MAPPING.remove(entry.bufferAllocator);
                return true;
            }
            return false;
        });
    }

    private static class Entry {

        private final BufferAllocator bufferAllocator;
        private long lastAccessTime;
        private boolean inUseOverMultipleFrames;

        public Entry(final BufferAllocator bufferAllocator) {
            this.bufferAllocator = bufferAllocator;
            this.lastAccessTime = System.currentTimeMillis();
        }

        public boolean shouldBeClosed() {
            return System.currentTimeMillis() - this.lastAccessTime > 60 * 1000;
        }

        public void onBorrow() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        public void onReturn() {
            this.bufferAllocator.reset();
            this.inUseOverMultipleFrames = false;
        }

    }

}
