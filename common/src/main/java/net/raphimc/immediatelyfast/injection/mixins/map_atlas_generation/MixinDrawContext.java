/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.MapRenderState;
import net.raphimc.immediatelyfast.injection.interfaces.IMapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture.ATLAS_SIZE;
import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture.MAP_SIZE;

@Mixin(DrawContext.class)
public abstract class MixinDrawContext {

    @WrapOperation(method = "drawMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexturedQuad(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lcom/mojang/blaze3d/textures/GpuTextureView;IIIIFFFFI)V", ordinal = 0))
    private void drawAtlasTexture(DrawContext instance, RenderPipeline pipeline, GpuTextureView texture, int x1, int y1, int x2, int y2, float u1, float u2, float v1, float v2, int color, Operation<Void> original, @Local(argsOnly = true) MapRenderState renderState) {
        final IMapRenderState immediatelyFast$renderState = (IMapRenderState) renderState;
        if (immediatelyFast$renderState.immediatelyFast$getAtlasTexture() != null && immediatelyFast$renderState.immediatelyFast$getAtlasTexture().getTextureId().equals(renderState.texture)) {
            u1 = (float) immediatelyFast$renderState.immediatelyFast$getAtlasX() / ATLAS_SIZE;
            u2 = (float) (immediatelyFast$renderState.immediatelyFast$getAtlasX() + MAP_SIZE) / ATLAS_SIZE;
            v1 = (float) immediatelyFast$renderState.immediatelyFast$getAtlasY() / ATLAS_SIZE;
            v2 = (float) (immediatelyFast$renderState.immediatelyFast$getAtlasY() + MAP_SIZE) / ATLAS_SIZE;
        }
        original.call(instance, pipeline, texture, x1, y1, x2, y2, u1, u2, v1, v2, color);
    }

}
