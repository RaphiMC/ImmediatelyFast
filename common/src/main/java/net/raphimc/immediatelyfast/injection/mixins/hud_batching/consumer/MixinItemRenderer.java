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
package net.raphimc.immediatelyfast.injection.mixins.hud_batching.consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ItemRenderer.class, priority = 1500)
public abstract class MixinItemRenderer {

    @ModifyArg(method = "renderGuiItemModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"))
    private VertexConsumerProvider renderItemIntoBuffer(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
        if (BatchingBuffers.LIT_ITEM_MODEL_CONSUMER != null || BatchingBuffers.UNLIT_ITEM_MODEL_CONSUMER != null) {
            // Get the model view transformations and apply them to the empty matrix stack.
            // When rendering that batch the model view matrix will be set to the identity matrix to not apply the model view transformations twice.
            matrices.peek().getPositionMatrix().set(RenderSystem.getModelViewMatrix());

            return model.isSideLit() ? BatchingBuffers.LIT_ITEM_MODEL_CONSUMER : BatchingBuffers.UNLIT_ITEM_MODEL_CONSUMER;
        }

        return vertexConsumers;
    }

    @ModifyArg(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"))
    private VertexConsumerProvider renderTextIntoBuffer(VertexConsumerProvider vertexConsumers) {
        return BatchingBuffers.ITEM_OVERLAY_CONSUMER != null ? BatchingBuffers.ITEM_OVERLAY_CONSUMER : vertexConsumers;
    }

    @Redirect(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawableHelper;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V"))
    private void renderQuadsIntoBuffer(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        if (BatchingBuffers.ITEM_OVERLAY_CONSUMER != null) {
            BatchingBuffers.beginItemOverlayRendering();
            DrawableHelper.fill(matrices, x1, y1, x2, y2, color);
            BatchingBuffers.endItemOverlayRendering();
            return;
        }

        DrawableHelper.fill(matrices, x1, y1, x2, y2, color);
    }

}
