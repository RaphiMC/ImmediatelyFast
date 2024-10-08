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
package net.raphimc.immediatelyfast.injection.mixins.sign_text_buffering;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.core.BufferAllocatorPool;
import net.raphimc.immediatelyfast.feature.sign_text_buffering.NoSetTextAnglesMatrixStack;
import net.raphimc.immediatelyfast.feature.sign_text_buffering.SignAtlasFramebuffer;
import net.raphimc.immediatelyfast.injection.interfaces.ISignText;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SignBlockEntityRenderer.class)
public abstract class MixinSignBlockEntityRenderer {

    @Shadow
    @Final
    private TextRenderer textRenderer;

    @Shadow
    abstract void renderText(BlockPos pos, SignText signText, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int lineHeight, int lineWidth, boolean front);

    @Shadow
    protected abstract void setTextAngles(MatrixStack matrices, boolean front, Vec3d translation);

    @Shadow
    abstract Vec3d getTextOffset();

    @Inject(method = "renderText", at = @At("HEAD"), cancellable = true)
    private void renderBufferedSignText(BlockPos pos, SignText signText, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int lineHeight, int lineWidth, boolean front, CallbackInfo ci) {
        if (matrices instanceof NoSetTextAnglesMatrixStack) return;
        final ISignText iSignText = (ISignText) signText;
        if (!iSignText.immediatelyFast$shouldCache()) return;

        SignAtlasFramebuffer.Slot slot = ImmediatelyFast.signTextCache.slotCache.getIfPresent(signText);
        if (slot == null) {
            final int width = this.immediatelyFast$getTextWidth(signText, lineWidth);
            final int height = 4 * lineHeight;
            if (width <= 0 || height <= 0) {
                iSignText.immediatelyFast$setShouldCache(false);
                return;
            }
            final int padding = signText.isGlowing() ? 2 : 0;

            slot = ImmediatelyFast.signTextCache.signAtlasFramebuffer.findSlot(width + padding, height + padding);
            if (slot != null) {
                final Matrix4f projectionMatrix = new Matrix4f().setOrtho(0F, SignAtlasFramebuffer.ATLAS_SIZE, SignAtlasFramebuffer.ATLAS_SIZE, 0F, 1000F, 21000F);
                RenderSystem.backupProjectionMatrix();
                RenderSystem.setProjectionMatrix(projectionMatrix, ProjectionType.ORTHOGRAPHIC);
                final Matrix4fStack modelViewMatrix = RenderSystem.getModelViewStack();
                modelViewMatrix.pushMatrix();
                modelViewMatrix.identity();
                modelViewMatrix.translate(0F, 0F, -11000F);
                final Fog fog = RenderSystem.getShaderFog();
                RenderSystem.setShaderFog(Fog.DUMMY);
                final BufferAllocator bufferAllocator = BufferAllocatorPool.borrowBufferAllocator();
                ImmediatelyFast.signTextCache.signAtlasFramebuffer.beginWrite(true);
                ImmediatelyFast.signTextCache.lockFramebuffer = true;

                try {
                    final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(bufferAllocator);
                    final MatrixStack matrixStack = new NoSetTextAnglesMatrixStack();
                    matrixStack.translate(slot.x, slot.y, 0F);
                    matrixStack.translate(slot.width / 2F, slot.height / 2F, 0F);
                    this.renderText(MinecraftClient.getInstance().cameraEntity.getBlockPos(), signText, matrixStack, immediate, light, lineHeight, lineWidth, front);
                    immediate.draw();
                } finally {
                    ImmediatelyFast.signTextCache.lockFramebuffer = false;
                    MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
                    BufferAllocatorPool.returnBufferAllocatorSafe(bufferAllocator);
                    RenderSystem.setShaderFog(fog);
                    modelViewMatrix.popMatrix();
                    RenderSystem.restoreProjectionMatrix();
                }

                ImmediatelyFast.signTextCache.slotCache.put(signText, slot);
            } else {
                ImmediatelyFast.LOGGER.warn("Failed to find a free slot for sign text (" + ImmediatelyFast.signTextCache.slotCache.size() + " sign texts in atlas). Falling back to immediate mode rendering.");
                iSignText.immediatelyFast$setShouldCache(false);
                return;
            }
        }

        float u1 = ((float) slot.x) / SignAtlasFramebuffer.ATLAS_SIZE;
        float u2 = ((float) slot.x + (float) slot.width) / SignAtlasFramebuffer.ATLAS_SIZE;
        float v1 = 1F - ((float) slot.y) / SignAtlasFramebuffer.ATLAS_SIZE;
        float v2 = 1F - ((float) slot.y + (float) slot.height) / SignAtlasFramebuffer.ATLAS_SIZE;

        if (signText.isGlowing()) {
            light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        }

        matrices.push();
        this.setTextAngles(matrices, front, this.getTextOffset());
        matrices.translate(-slot.width / 2F, -slot.height / 2F, 0F);
        final Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        final VertexConsumer vertexConsumer = vertexConsumers.getBuffer(ImmediatelyFast.signTextCache.renderLayer);
        vertexConsumer.vertex(matrix4f, 0F, slot.height, 0F).color(255, 255, 255, 255).texture(u1, v2).light(light);
        vertexConsumer.vertex(matrix4f, slot.width, slot.height, 0F).color(255, 255, 255, 255).texture(u2, v2).light(light);
        vertexConsumer.vertex(matrix4f, slot.width, 0F, 0F).color(255, 255, 255, 255).texture(u2, v1).light(light);
        vertexConsumer.vertex(matrix4f, 0F, 0F, 0F).color(255, 255, 255, 255).texture(u1, v1).light(light);
        matrices.pop();

        ci.cancel();
    }

    @Redirect(method = "renderText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/SignBlockEntityRenderer;setTextAngles(Lnet/minecraft/client/util/math/MatrixStack;ZLnet/minecraft/util/math/Vec3d;)V"))
    private void dontSetTextAngles(SignBlockEntityRenderer instance, MatrixStack matrices, boolean front, Vec3d translation) {
        if (matrices instanceof NoSetTextAnglesMatrixStack) return;

        this.setTextAngles(matrices, front, translation);
    }

    @Unique
    private int immediatelyFast$getTextWidth(final SignText signText, final int lineWidth) {
        final OrderedText[] orderedTexts = signText.getOrderedMessages(MinecraftClient.getInstance().shouldFilterText(), text -> {
            final List<OrderedText> list = this.textRenderer.wrapLines(text, lineWidth);
            return list.isEmpty() ? OrderedText.EMPTY : list.get(0);
        });

        int width = 0;
        for (OrderedText orderedText : orderedTexts) {
            width = Math.max(width, this.textRenderer.getWidth(orderedText));
        }
        if (width % 2 != 0) width++; // Fixes issue which squishes the text when the width is odd (Test text: "hhhl")

        return width;
    }

}
