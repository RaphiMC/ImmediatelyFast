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
package net.raphimc.immediatelyfast.injection.mixins.sign_text_buffering;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.core.ByteBufferBuilderPool;
import net.raphimc.immediatelyfast.feature.sign_text_buffering.NoTextTransformPoseStack;
import net.raphimc.immediatelyfast.feature.sign_text_buffering.SignAtlasRenderTarget;
import net.raphimc.immediatelyfast.injection.interfaces.ISignText;
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

@Mixin(AbstractSignRenderer.class)
public abstract class MixinAbstractSignRenderer {

    @Shadow
    @Final
    private Font font;

    @Shadow
    protected abstract void submitSignText(SignRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, boolean isFront);

    @Shadow
    protected abstract void translateSignText(PoseStack poseStack, boolean isFront, Vec3 offset);

    @Shadow
    protected abstract Vec3 getTextOffset();

    @Inject(method = "submitSignText", at = @At("HEAD"), cancellable = true)
    private void renderBufferedSignText(SignRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, boolean isFront, CallbackInfo ci) {
        if (poseStack instanceof NoTextTransformPoseStack) return;
        final SignText signText = isFront ? renderState.frontText : renderState.backText;
        if (!(signText instanceof ISignText mixinSignText)) return;
        if (!mixinSignText.immediatelyFast$shouldCache()) return;

        SignAtlasRenderTarget.Slot slot = ImmediatelyFast.signTextCache.slotCache.getIfPresent(signText);
        if (slot == null) {
            final int width = this.immediatelyFast$getTextWidth(signText, renderState.isTextFilteringEnabled, renderState.maxTextLineWidth);
            final int height = 4 * renderState.textLineHeight;
            if (width <= 0 || height <= 0) {
                mixinSignText.immediatelyFast$setShouldCache(false);
                return;
            }
            final int padding = signText.hasGlowingText() ? 2 : 0;

            slot = ImmediatelyFast.signTextCache.signAtlasRenderTarget.findSlot(width + padding, height + padding);
            if (slot != null) {
                RenderSystem.backupProjectionMatrix();
                RenderSystem.setProjectionMatrix(ImmediatelyFast.signTextCache.signProjectionMatrix, ProjectionType.ORTHOGRAPHIC);
                final Matrix4fStack modelViewMatrix = RenderSystem.getModelViewStack();
                modelViewMatrix.pushMatrix().identity();
                final GpuBufferSlice fog = RenderSystem.getShaderFog();
                RenderSystem.setShaderFog(Minecraft.getInstance().gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
                final GpuTextureView previousColorTextureOverride = RenderSystem.outputColorTextureOverride;
                final GpuTextureView previousDepthTextureOverride = RenderSystem.outputDepthTextureOverride;
                RenderSystem.outputColorTextureOverride = ImmediatelyFast.signTextCache.signAtlasRenderTarget.getColorTextureView();
                RenderSystem.outputDepthTextureOverride = ImmediatelyFast.signTextCache.signAtlasRenderTarget.getDepthTextureView();
                final ByteBufferBuilder bufferBuilder = ByteBufferBuilderPool.borrowBufferBuilder();

                try {
                    final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(bufferBuilder);
                    final SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
                    final FeatureRenderDispatcher renderDispatcher = new FeatureRenderDispatcher(submitNodeStorage, Minecraft.getInstance().getBlockRenderer(), bufferSource, Minecraft.getInstance().getAtlasManager(), null, null, this.font);
                    final PoseStack textPoseStack = new NoTextTransformPoseStack();
                    textPoseStack.translate(slot.x, slot.y, 0F);
                    textPoseStack.translate(slot.width / 2F, slot.height / 2F, 0F);
                    renderState.drawOutline = true; // Always render outline, regardless of distance to sign
                    this.submitSignText(renderState, textPoseStack, submitNodeStorage, isFront);
                    renderDispatcher.renderAllFeatures();
                    bufferSource.endBatch();
                    renderDispatcher.close();
                } finally {
                    ByteBufferBuilderPool.returnBufferBuilderSafe(bufferBuilder);
                    RenderSystem.outputColorTextureOverride = previousColorTextureOverride;
                    RenderSystem.outputDepthTextureOverride = previousDepthTextureOverride;
                    RenderSystem.setShaderFog(fog);
                    modelViewMatrix.popMatrix();
                    RenderSystem.restoreProjectionMatrix();
                }

                ImmediatelyFast.signTextCache.slotCache.put(signText, slot);
            } else {
                ImmediatelyFast.LOGGER.warn("Failed to find a free slot for sign text (" + ImmediatelyFast.signTextCache.slotCache.size() + " sign texts in atlas). Falling back to immediate mode rendering.");
                mixinSignText.immediatelyFast$setShouldCache(false);
                return;
            }
        }

        final float u1 = ((float) slot.x) / SignAtlasRenderTarget.ATLAS_SIZE;
        final float u2 = ((float) slot.x + (float) slot.width) / SignAtlasRenderTarget.ATLAS_SIZE;
        final float v1 = 1F - ((float) slot.y) / SignAtlasRenderTarget.ATLAS_SIZE;
        final float v2 = 1F - ((float) slot.y + (float) slot.height) / SignAtlasRenderTarget.ATLAS_SIZE;
        final int light = signText.hasGlowingText() ? LightTexture.FULL_BRIGHT : renderState.lightCoords;

        poseStack.pushPose();
        this.translateSignText(poseStack, isFront, this.getTextOffset());
        poseStack.translate(-slot.width / 2F, -slot.height / 2F, 0F);
        final SignAtlasRenderTarget.Slot finalSlot = slot;
        nodeCollector.submitCustomGeometry(poseStack, ImmediatelyFast.signTextCache.renderType, (entry, vertexConsumer) -> {
            vertexConsumer.addVertex(entry, 0F, finalSlot.height, 0F).setColor(255, 255, 255, 255).setUv(u1, v2).setLight(light);
            vertexConsumer.addVertex(entry, finalSlot.width, finalSlot.height, 0F).setColor(255, 255, 255, 255).setUv(u2, v2).setLight(light);
            vertexConsumer.addVertex(entry, finalSlot.width, 0F, 0F).setColor(255, 255, 255, 255).setUv(u2, v1).setLight(light);
            vertexConsumer.addVertex(entry, 0F, 0F, 0F).setColor(255, 255, 255, 255).setUv(u1, v1).setLight(light);
        });
        poseStack.popPose();

        ci.cancel();
    }

    @Redirect(method = "submitSignText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/AbstractSignRenderer;translateSignText(Lcom/mojang/blaze3d/vertex/PoseStack;ZLnet/minecraft/world/phys/Vec3;)V"))
    private void dontApplyTextTransform(AbstractSignRenderer instance, PoseStack poseStack, boolean isFront, Vec3 offset) {
        if (poseStack instanceof NoTextTransformPoseStack) return;

        this.translateSignText(poseStack, isFront, offset);
    }

    @Unique
    private int immediatelyFast$getTextWidth(final SignText signText, final boolean filterText, final int maxLineWidth) {
        final FormattedCharSequence[] renderMessages = signText.getRenderMessages(filterText, textComponent -> {
            final List<FormattedCharSequence> list = this.font.split(textComponent, maxLineWidth);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.getFirst();
        });

        int width = 0;
        for (FormattedCharSequence line : renderMessages) {
            width = Math.max(width, this.font.width(line));
        }
        if (width % 2 != 0) width++; // Fixes issue which squishes the text when the width is odd (Test text: "hhhl")

        return width;
    }

}
