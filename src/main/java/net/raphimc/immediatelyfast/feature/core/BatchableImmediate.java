/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;

import java.util.Map;

public class BatchableImmediate extends ImmediateAdapter {

    public BatchableImmediate() {
    }

    public BatchableImmediate(final Map<RenderLayer, BufferBuilder> layerBuffers) {
        super(layerBuffers);
    }

    @Override
    protected void _draw(final RenderLayer layer) {
        for (BufferBuilder bufferBuilder : this.getBufferBuilder(layer)) {
            if (bufferBuilder != null) layer.draw(bufferBuilder, RenderSystem.getVertexSorting());
        }
    }

}
