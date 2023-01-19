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
package net.raphimc.immediatelyfast.injection.mixins.fast_buffer_upload;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.raphimc.immediatelyfast.injection.interfaces.IVertexFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(VertexFormat.class)
public class MixinVertexFormat implements IVertexFormat {
    @Shadow @Mutable @Final private int size;
    @Shadow @Mutable @Final private ImmutableList<VertexFormatElement> elements;
    @Shadow @Mutable @Final private IntList offsets;


    @Override
    public void setUpState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::setUpStateInternal);
        } else {
            setUpStateInternal();
        }
    }

    @Override
    public void setUpStateInternal() {
        int i = size;
        List<VertexFormatElement> list = elements;

        for(int j = 0; j < list.size(); ++j) {
            list.get(j).startDrawing(j, this.offsets.getInt(j), i);
        }
    }

    @Override
    public void clearState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::clearStateInternal);
        } else {
            this.clearStateInternal();
        }
    }

    @Override
    public void clearStateInternal() {
        ImmutableList<VertexFormatElement> immutableList = elements;

        for(int i = 0; i < immutableList.size(); ++i) {
            VertexFormatElement vertexFormatElement = immutableList.get(i);
            vertexFormatElement.endDrawing((i));
        }
    }
}
