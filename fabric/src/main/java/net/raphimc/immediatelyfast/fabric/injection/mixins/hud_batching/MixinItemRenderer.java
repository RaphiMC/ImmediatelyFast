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
package net.raphimc.immediatelyfast.fabric.injection.mixins.hud_batching;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.raphimc.immediatelyfast.feature.batching.HudBatchingBufferSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @WrapMethod(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;Z)V")
    private void renderItem(ItemStack stack, ModelTransformationMode transformationMode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, boolean useInventoryModel, Operation<Void> original) {
        if (vertexConsumers instanceof HudBatchingBufferSource hudBatchingBufferSource) {
            hudBatchingBufferSource.setRenderingItem(true);
        }
        try {
            original.call(stack, transformationMode, matrices, vertexConsumers, light, overlay, model, useInventoryModel);
        } finally {
            if (vertexConsumers instanceof HudBatchingBufferSource hudBatchingBufferSource) {
                hudBatchingBufferSource.setRenderingItem(false);
            }
        }
    }

}
