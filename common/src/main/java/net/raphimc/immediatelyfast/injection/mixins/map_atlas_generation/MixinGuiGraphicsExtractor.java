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
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.MapRenderState;
import net.raphimc.immediatelyfast.injection.interfaces.IMapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture.ATLAS_SIZE;
import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture.MAP_SIZE;

@Mixin(GuiGraphicsExtractor.class)
public abstract class MixinGuiGraphicsExtractor {

    @WrapOperation(method = "map", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;innerBlit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lcom/mojang/blaze3d/textures/GpuTextureView;Lcom/mojang/blaze3d/textures/GpuSampler;IIIIFFFFI)V", ordinal = 0))
    private void modifyTextureCoordinates(final GuiGraphicsExtractor instance, final RenderPipeline pipeline, final GpuTextureView textureView, final GpuSampler sampler, final int x0, final int y0, final int x1, final int y1, float u0, float u1, float v0, float v1, final int color, final Operation<Void> original, @Local(name = "mapRenderState", argsOnly = true) final MapRenderState mapRenderState) {
        final IMapRenderState immediatelyFast$mapRenderState = (IMapRenderState) mapRenderState;
        if (immediatelyFast$mapRenderState.immediatelyFast$getAtlasTexture() != null && immediatelyFast$mapRenderState.immediatelyFast$getAtlasTexture().getTextureId().equals(mapRenderState.texture)) {
            u0 = (float) immediatelyFast$mapRenderState.immediatelyFast$getAtlasX() / ATLAS_SIZE;
            u1 = (float) (immediatelyFast$mapRenderState.immediatelyFast$getAtlasX() + MAP_SIZE) / ATLAS_SIZE;
            v0 = (float) immediatelyFast$mapRenderState.immediatelyFast$getAtlasY() / ATLAS_SIZE;
            v1 = (float) (immediatelyFast$mapRenderState.immediatelyFast$getAtlasY() + MAP_SIZE) / ATLAS_SIZE;
        }
        original.call(instance, pipeline, textureView, sampler, x0, y0, x1, y1, u0, u1, v0, v1, color);
    }

}
