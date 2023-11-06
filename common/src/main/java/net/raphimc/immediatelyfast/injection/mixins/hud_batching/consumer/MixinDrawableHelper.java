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
package net.raphimc.immediatelyfast.injection.mixins.hud_batching.consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.lenni0451.reflect.Objects;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import net.raphimc.immediatelyfast.feature.batching.BatchingRenderLayers;
import net.raphimc.immediatelyfast.feature.batching.BlendFuncDepthFunc;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DrawableHelper.class, priority = 500)
public abstract class MixinDrawableHelper {

    @Shadow
    protected static void fillGradient(Matrix4f matrix, BufferBuilder builder, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z) {
    }

    @Inject(method = "fill(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", at = @At("HEAD"), cancellable = true)
    private static void fillIntoBuffer(MatrixStack matrices, int x1, int y1, int x2, int y2, int z, int color, CallbackInfo ci) {
        if (BatchingBuffers.FILL_CONSUMER != null) {
            final Matrix4f matrix = matrices.peek().getPositionMatrix();

            ci.cancel();
            if (x1 < x2) {
                x1 = x1 ^ x2;
                x2 = x1 ^ x2;
                x1 = x1 ^ x2;
            }
            if (y1 < y2) {
                y1 = y1 ^ y2;
                y2 = y1 ^ y2;
                y1 = y1 ^ y2;
            }
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int argb = (int) (shaderColor[3] * 255) << 24 | (int) (shaderColor[0] * 255) << 16 | (int) (shaderColor[1] * 255) << 8 | (int) (shaderColor[2] * 255);
            color = ColorHelper.Argb.mixColor(color, argb);

            RenderSystem.enableBlend();
            final VertexConsumer vertexConsumer = BatchingBuffers.FILL_CONSUMER.getBuffer(BatchingRenderLayers.FILLED_QUAD.apply(BlendFuncDepthFunc.current()));
            vertexConsumer.vertex(matrix, x1, y2, 0F).color(color).next();
            vertexConsumer.vertex(matrix, x2, y2, 0F).color(color).next();
            vertexConsumer.vertex(matrix, x2, y1, 0F).color(color).next();
            vertexConsumer.vertex(matrix, x1, y1, 0F).color(color).next();
            RenderSystem.disableBlend();
        }
    }

    @Inject(method = "fillGradient(Lnet/minecraft/client/util/math/MatrixStack;IIIIIII)V", at = @At("HEAD"), cancellable = true)
    private static void fillIntoBuffer(MatrixStack matrices, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z, CallbackInfo ci) {
        if (BatchingBuffers.FILL_CONSUMER != null) {
            ci.cancel();
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int argb = (int) (shaderColor[3] * 255) << 24 | (int) (shaderColor[0] * 255) << 16 | (int) (shaderColor[1] * 255) << 8 | (int) (shaderColor[2] * 255);
            colorStart = ColorHelper.Argb.mixColor(colorStart, argb);
            colorEnd = ColorHelper.Argb.mixColor(colorEnd, argb);
            RenderSystem.enableBlend();
            final VertexConsumer vertexConsumer = BatchingBuffers.FILL_CONSUMER.getBuffer(BatchingRenderLayers.FILLED_QUAD.apply(BlendFuncDepthFunc.current()));
            fillGradient(matrices.peek().getPositionMatrix(), Objects.cast(vertexConsumer, BufferBuilder.class), startX, startY, endX, endY, z, colorStart, colorEnd);
            RenderSystem.disableBlend();
        }
    }

    @Inject(method = "drawTexturedQuad(Lorg/joml/Matrix4f;IIIIIFFFF)V", at = @At("HEAD"), cancellable = true)
    private static void drawTexturedQuadIntoBuffer(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, CallbackInfo ci) {
        if (BatchingBuffers.TEXTURE_CONSUMER != null) {
            ci.cancel();
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int r = (int) (shaderColor[0] * 255);
            final int g = (int) (shaderColor[1] * 255);
            final int b = (int) (shaderColor[2] * 255);
            final int a = (int) (shaderColor[3] * 255);
            final VertexConsumer vertexConsumer = BatchingBuffers.TEXTURE_CONSUMER.getBuffer(BatchingRenderLayers.COLORED_TEXTURE.apply(RenderSystem.getShaderTexture(0), BlendFuncDepthFunc.current()));
            vertexConsumer.vertex(matrix, x0, y1, z).texture(u0, v1).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x1, y1, z).texture(u1, v1).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x1, y0, z).texture(u1, v0).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x0, y0, z).texture(u0, v0).color(r, g, b, a).next();
        }
    }

    @Inject(method = "drawTexturedQuad(Lorg/joml/Matrix4f;IIIIIFFFFFFFF)V", at = @At("HEAD"), cancellable = true)
    private static void drawTexturedQuadIntoBuffer(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, float red, float green, float blue, float alpha, CallbackInfo ci) {
        if (BatchingBuffers.TEXTURE_CONSUMER != null) {
            ci.cancel();
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int argb = (int) (shaderColor[3] * 255) << 24 | (int) (shaderColor[0] * 255) << 16 | (int) (shaderColor[1] * 255) << 8 | (int) (shaderColor[2] * 255);
            final int color = ColorHelper.Argb.mixColor((int) (alpha * 255) << 24 | (int) (red * 255) << 16 | (int) (green * 255) << 8 | (int) (blue * 255), argb);

            RenderSystem.enableBlend();
            final VertexConsumer vertexConsumer = BatchingBuffers.TEXTURE_CONSUMER.getBuffer(BatchingRenderLayers.COLORED_TEXTURE.apply(RenderSystem.getShaderTexture(0), BlendFuncDepthFunc.current()));
            vertexConsumer.vertex(matrix, x0, y1, z).texture(u0, v1).color(color).next();
            vertexConsumer.vertex(matrix, x1, y1, z).texture(u1, v1).color(color).next();
            vertexConsumer.vertex(matrix, x1, y0, z).texture(u1, v0).color(color).next();
            vertexConsumer.vertex(matrix, x0, y0, z).texture(u0, v0).color(color).next();
            RenderSystem.disableBlend();
        }
    }

}
