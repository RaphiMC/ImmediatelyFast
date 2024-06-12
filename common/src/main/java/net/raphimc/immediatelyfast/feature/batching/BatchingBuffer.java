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
package net.raphimc.immediatelyfast.feature.batching;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import net.raphimc.immediatelyfast.feature.core.BatchableBufferSource;

import java.util.SequencedMap;

public class BatchingBuffer extends BatchableBufferSource {

    public static boolean IS_DRAWING;

    public BatchingBuffer() {
    }

    public BatchingBuffer(final SequencedMap<RenderLayer, BufferAllocator> layerBuffers) {
        super(layerBuffers);
    }

    public BatchingBuffer(final BufferAllocator fallbackBuffer, final SequencedMap<RenderLayer, BufferAllocator> layerBuffers) {
        super(fallbackBuffer, layerBuffers);
    }

    @Override
    public void draw(final RenderLayer layer) {
        try {
            IS_DRAWING = true;
            super.draw(layer);
        } finally {
            IS_DRAWING = false;
        }
    }

}
