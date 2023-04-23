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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import net.raphimc.immediatelyfast.feature.batching.BatchingRenderLayers;
import net.raphimc.immediatelyfast.feature.batching.BlendFuncDepthFunc;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DrawContext.class, priority = 1500)
public abstract class MixinDrawContext {

    @Shadow
    @Final
    private MatrixStack matrices;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "fill(IIIIII)V", at = @At("HEAD"), cancellable = true)
    private void fillIntoBuffer(int x1, int y1, int x2, int y2, int z, int color, CallbackInfo ci) {
        if (BatchingBuffers.FILL_CONSUMER != null) {
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
            final Matrix4f matrix = this.matrices.peek().getPositionMatrix();
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int argb = (int) (shaderColor[3] * 255) << 24 | (int) (shaderColor[0] * 255) << 16 | (int) (shaderColor[1] * 255) << 8 | (int) (shaderColor[2] * 255);
            color = ColorHelper.Argb.mixColor(color, argb);

            RenderSystem.enableBlend();
            final VertexConsumer vertexConsumer = BatchingBuffers.FILL_CONSUMER.getBuffer(BatchingRenderLayers.FILLED_QUAD.apply(BlendFuncDepthFunc.current()));
            vertexConsumer.vertex(matrix, x1, y2, z).color(color).next();
            vertexConsumer.vertex(matrix, x2, y2, z).color(color).next();
            vertexConsumer.vertex(matrix, x2, y1, z).color(color).next();
            vertexConsumer.vertex(matrix, x1, y1, z).color(color).next();
            RenderSystem.disableBlend();
        }
    }

    @Inject(method = "drawTexturedQuad(Lnet/minecraft/util/Identifier;IIIIIFFFF)V", at = @At("HEAD"), cancellable = true)
    private void drawTexturedQuadIntoBuffer(Identifier texture, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, CallbackInfo ci) {
        if (BatchingBuffers.TEXTURE_CONSUMER != null) {
            ci.cancel();
            final Matrix4f matrix = this.matrices.peek().getPositionMatrix();
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int r = (int) (shaderColor[0] * 255);
            final int g = (int) (shaderColor[1] * 255);
            final int b = (int) (shaderColor[2] * 255);
            final int a = (int) (shaderColor[3] * 255);
            final VertexConsumer vertexConsumer = BatchingBuffers.TEXTURE_CONSUMER.getBuffer(BatchingRenderLayers.COLORED_TEXTURE.apply(this.client.getTextureManager().getTexture(texture).getGlId(), BlendFuncDepthFunc.current()));
            vertexConsumer.vertex(matrix, x0, y1, z).texture(u0, v1).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x1, y1, z).texture(u1, v1).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x1, y0, z).texture(u1, v0).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x0, y0, z).texture(u0, v0).color(r, g, b, a).next();
        }
    }

    @Inject(method = "drawTexturedQuad(Lnet/minecraft/util/Identifier;IIIIIFFFFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void drawTexturedQuadIntoBuffer(Identifier texture, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, float red, float green, float blue, float alpha, CallbackInfo ci) {
        if (BatchingBuffers.TEXTURE_CONSUMER != null) {
            ci.cancel();
            final Matrix4f matrix = this.matrices.peek().getPositionMatrix();
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int argb = (int) (shaderColor[3] * 255) << 24 | (int) (shaderColor[0] * 255) << 16 | (int) (shaderColor[1] * 255) << 8 | (int) (shaderColor[2] * 255);
            final int color = ColorHelper.Argb.mixColor((int) (alpha * 255) << 24 | (int) (red * 255) << 16 | (int) (green * 255) << 8 | (int) (blue * 255), argb);

            RenderSystem.enableBlend();
            final VertexConsumer vertexConsumer = BatchingBuffers.TEXTURE_CONSUMER.getBuffer(BatchingRenderLayers.COLORED_TEXTURE.apply(this.client.getTextureManager().getTexture(texture).getGlId(), BlendFuncDepthFunc.current()));
            vertexConsumer.vertex(matrix, x0, y1, z).texture(u0, v1).color(color).next();
            vertexConsumer.vertex(matrix, x1, y1, z).texture(u1, v1).color(color).next();
            vertexConsumer.vertex(matrix, x1, y0, z).texture(u1, v0).color(color).next();
            vertexConsumer.vertex(matrix, x0, y0, z).texture(u0, v0).color(color).next();
            RenderSystem.disableBlend();
        }
    }

    @ModifyArg(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"))
    private VertexConsumerProvider renderTextIntoBuffer1(VertexConsumerProvider vertexConsumers) {
        return BatchingBuffers.TEXT_CONSUMER != null ? BatchingBuffers.TEXT_CONSUMER : vertexConsumers;
    }

    @ModifyArg(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I"))
    private VertexConsumerProvider renderTextIntoBuffer2(VertexConsumerProvider vertexConsumers) {
        return BatchingBuffers.TEXT_CONSUMER != null ? BatchingBuffers.TEXT_CONSUMER : vertexConsumers;
    }

    @ModifyArg(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"))
    private VertexConsumerProvider renderItemIntoBuffer(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
        if (BatchingBuffers.LIT_ITEM_MODEL_CONSUMER != null || BatchingBuffers.UNLIT_ITEM_MODEL_CONSUMER != null) {
            return model.isSideLit() ? BatchingBuffers.LIT_ITEM_MODEL_CONSUMER : BatchingBuffers.UNLIT_ITEM_MODEL_CONSUMER;
        }

        return vertexConsumers;
    }

    @Unique
    private VertexConsumerProvider prevFillConsumer = null;

    @Unique
    private VertexConsumerProvider prevTextConsumer = null;

    @Unique
    private VertexConsumerProvider prevTextureConsumer = null;

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    private void renderItemOverlayIntoBufferStart(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (BatchingBuffers.ITEM_OVERLAY_CONSUMER != null) {
            this.prevFillConsumer = BatchingBuffers.FILL_CONSUMER;
            this.prevTextConsumer = BatchingBuffers.TEXT_CONSUMER;
            this.prevTextureConsumer = BatchingBuffers.TEXTURE_CONSUMER;
            BatchingBuffers.FILL_CONSUMER = BatchingBuffers.ITEM_OVERLAY_CONSUMER;
            BatchingBuffers.TEXT_CONSUMER = BatchingBuffers.ITEM_OVERLAY_CONSUMER;
            BatchingBuffers.TEXTURE_CONSUMER = BatchingBuffers.ITEM_OVERLAY_CONSUMER;
        }
    }

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("RETURN"))
    private void renderItemOverlayIntoBufferEnd(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (BatchingBuffers.ITEM_OVERLAY_CONSUMER != null) {
            BatchingBuffers.FILL_CONSUMER = this.prevFillConsumer;
            BatchingBuffers.TEXT_CONSUMER = this.prevTextConsumer;
            BatchingBuffers.TEXTURE_CONSUMER = this.prevTextureConsumer;
        }
    }

}
