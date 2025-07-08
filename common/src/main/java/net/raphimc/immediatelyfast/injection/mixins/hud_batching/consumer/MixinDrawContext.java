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
package net.raphimc.immediatelyfast.injection.mixins.hud_batching.consumer;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.raphimc.immediatelyfast.feature.batching.BatchingRenderLayers;
import net.raphimc.immediatelyfast.feature.batching.BlendFuncDepthFuncState;
import net.raphimc.immediatelyfast.feature.batching.HudBatchingBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DrawContext.class, priority = 1500)
public abstract class MixinDrawContext {

    @Shadow
    @Final
    private MatrixStack matrices;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Mutable
    public VertexConsumerProvider.Immediate vertexConsumers;

    @ModifyVariable(method = "fill(Lnet/minecraft/client/render/RenderLayer;IIIIII)V", at = @At("HEAD"), index = 6, argsOnly = true)
    private int mixColor(int color) {
        if (this.vertexConsumers instanceof HudBatchingBufferSource) {
            return this.immediatelyFast$mixWithShaderColor(color);
        }
        return color;
    }

    @ModifyVariable(method = "fillGradient(Lnet/minecraft/client/render/RenderLayer;IIIIIII)V", at = @At("HEAD"), index = 5, argsOnly = true)
    private int mixStartColor(int color) {
        if (this.vertexConsumers instanceof HudBatchingBufferSource) {
            return this.immediatelyFast$mixWithShaderColor(color);
        }
        return color;
    }

    @ModifyVariable(method = "fillGradient(Lnet/minecraft/client/render/RenderLayer;IIIIIII)V", at = @At("HEAD"), index = 6, argsOnly = true)
    private int mixEndColor(int color) {
        if (this.vertexConsumers instanceof HudBatchingBufferSource) {
            return this.immediatelyFast$mixWithShaderColor(color);
        }
        return color;
    }

    @Inject(method = "drawTexturedQuad(Lnet/minecraft/util/Identifier;IIIIIFFFF)V", at = @At("HEAD"), cancellable = true)
    private void drawTexturedQuadIntoBuffer(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, CallbackInfo ci) {
        if (this.vertexConsumers instanceof HudBatchingBufferSource) {
            ci.cancel();
            final Matrix4f matrix = this.matrices.peek().getPositionMatrix();
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int r = MathHelper.clamp((int) (shaderColor[0] * 255), 0, 255);
            final int g = MathHelper.clamp((int) (shaderColor[1] * 255), 0, 255);
            final int b = MathHelper.clamp((int) (shaderColor[2] * 255), 0, 255);
            final int a = MathHelper.clamp((int) (shaderColor[3] * 255), 0, 255);
            if (r == 255 && g == 255 && b == 255 && a == 255) {
                final VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(BatchingRenderLayers.TEXTURE.apply(this.client.getTextureManager().getTexture(texture).getGlId(), BlendFuncDepthFuncState.current()));
                vertexConsumer.vertex(matrix, x1, y2, z).texture(u1, v2);
                vertexConsumer.vertex(matrix, x2, y2, z).texture(u2, v2);
                vertexConsumer.vertex(matrix, x2, y1, z).texture(u2, v1);
                vertexConsumer.vertex(matrix, x1, y1, z).texture(u1, v1);
            } else {
                final VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(BatchingRenderLayers.COLORED_TEXTURE.apply(this.client.getTextureManager().getTexture(texture).getGlId(), BlendFuncDepthFuncState.current()));
                vertexConsumer.vertex(matrix, x1, y2, z).texture(u1, v2).color(r, g, b, a);
                vertexConsumer.vertex(matrix, x2, y2, z).texture(u2, v2).color(r, g, b, a);
                vertexConsumer.vertex(matrix, x2, y1, z).texture(u2, v1).color(r, g, b, a);
                vertexConsumer.vertex(matrix, x1, y1, z).texture(u1, v1).color(r, g, b, a);
            }
        }
    }

    @Inject(method = "drawTexturedQuad(Lnet/minecraft/util/Identifier;IIIIIFFFFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void drawTexturedQuadIntoBuffer(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha, CallbackInfo ci) {
        if (this.vertexConsumers instanceof HudBatchingBufferSource) {
            ci.cancel();
            final Matrix4f matrix = this.matrices.peek().getPositionMatrix();
            final int color = this.immediatelyFast$mixWithShaderColor((int) (alpha * 255) << 24 | (int) (red * 255) << 16 | (int) (green * 255) << 8 | (int) (blue * 255));

            RenderSystem.enableBlend();
            final VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(BatchingRenderLayers.COLORED_TEXTURE.apply(this.client.getTextureManager().getTexture(texture).getGlId(), BlendFuncDepthFuncState.current()));
            vertexConsumer.vertex(matrix, x1, y2, z).texture(u1, v2).color(color);
            vertexConsumer.vertex(matrix, x2, y2, z).texture(u2, v2).color(color);
            vertexConsumer.vertex(matrix, x2, y1, z).texture(u2, v1).color(color);
            vertexConsumer.vertex(matrix, x1, y1, z).texture(u1, v1).color(color);
            RenderSystem.disableBlend();
        }
    }

    @Inject(method = "tryDraw", at = @At("HEAD"), cancellable = true)
    private void dontTryDrawIfBatching(CallbackInfo ci) {
        if (this.vertexConsumers instanceof HudBatchingBufferSource) {
            ci.cancel();
        }
    }

    @WrapWithCondition(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V"))
    private boolean dontDrawIfBatching(DrawContext instance) {
        return !(instance.vertexConsumers instanceof HudBatchingBufferSource);
    }

    @Unique
    private int immediatelyFast$mixWithShaderColor(final int color) {
        final float[] shaderColor = RenderSystem.getShaderColor();
        final int argb = MathHelper.clamp((int) (shaderColor[3] * 255), 0, 255) << 24
                | MathHelper.clamp((int) (shaderColor[0] * 255), 0, 255) << 16
                | MathHelper.clamp((int) (shaderColor[1] * 255), 0, 255) << 8
                | MathHelper.clamp((int) (shaderColor[2] * 255), 0, 255);
        return ColorHelper.Argb.mixColor(color, argb);
    }

}
