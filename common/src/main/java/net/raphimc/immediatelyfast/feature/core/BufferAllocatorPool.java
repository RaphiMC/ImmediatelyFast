/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.minecraft.client.util.BufferAllocator;
import net.raphimc.immediatelyfast.ImmediatelyFast;

public class BufferAllocatorPool {

    private static final ReferenceList<BufferAllocator> FREE = new ReferenceArrayList<>();
    private static final ReferenceList<BufferAllocator> IN_USE = new ReferenceArrayList<>();
    private static final Reference2LongMap<BufferAllocator> BUFFER_ALLOCATOR_ACCESS_TIME = new Reference2LongOpenHashMap<>();

    private BufferAllocatorPool() {
    }

    public static BufferAllocator borrowBufferAllocator() {
        RenderSystem.assertOnRenderThread();
        BufferAllocator bufferAllocator;
        if (FREE.isEmpty()) {
            bufferAllocator = new BufferAllocator(256);
        } else {
            bufferAllocator = FREE.removeFirst();
            if (bufferAllocator.pointer == 0L) { // If the buffer was closed while in the pool
                BUFFER_ALLOCATOR_ACCESS_TIME.removeLong(bufferAllocator);
                bufferAllocator = new BufferAllocator(256);
            }
        }
        IN_USE.add(bufferAllocator);
        BUFFER_ALLOCATOR_ACCESS_TIME.put(bufferAllocator, System.currentTimeMillis());
        return bufferAllocator;
    }

    public static void returnBufferAllocatorSafe(final BufferAllocator bufferAllocator) {
        RenderSystem.assertOnRenderThread();
        if (!IN_USE.remove(bufferAllocator)) {
            return;
        }
        bufferAllocator.reset();
        FREE.add(bufferAllocator);
    }

    public static int getSize() {
        return FREE.size() + IN_USE.size();
    }

    public static void onEndFrame() {
        if (!IN_USE.isEmpty()) {
            ImmediatelyFast.LOGGER.warn(IN_USE.size() + " BufferAllocator(s) were not returned to the pool. Forcibly reclaiming them.");
            for (BufferAllocator bufferAllocator : IN_USE) {
                bufferAllocator.reset();
            }
            FREE.addAll(IN_USE);
            IN_USE.clear();
        }
        BUFFER_ALLOCATOR_ACCESS_TIME.reference2LongEntrySet().removeIf(entry -> {
            if (System.currentTimeMillis() - entry.getLongValue() > 60 * 1000) {
                if (FREE.contains(entry.getKey())) {
                    FREE.remove(entry.getKey());
                    entry.getKey().close();
                }
                return true;
            }
            return false;
        });
    }

}
