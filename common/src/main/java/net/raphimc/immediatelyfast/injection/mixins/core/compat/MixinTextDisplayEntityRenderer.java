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
package net.raphimc.immediatelyfast.injection.mixins.core.compat;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.DisplayEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayEntityRenderer.TextDisplayEntityRenderer.class)
public abstract class MixinTextDisplayEntityRenderer {

    /**
     * Fixes <a href="https://github.com/RaphiMC/ImmediatelyFast/issues/265">https://github.com/RaphiMC/ImmediatelyFast/issues/265</a>
     * Needed because the universal batching optimization may render text before the background, which causes the see through background to be rendered over the text (probably due to polygon offset).
     */
    @Inject(method = "render(Lnet/minecraft/entity/decoration/DisplayEntity$TextDisplayEntity;Lnet/minecraft/entity/decoration/DisplayEntity$TextDisplayEntity$Data;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/DisplayEntity$TextDisplayEntity$TextLines;lines()Ljava/util/List;", ordinal = 1))
    private void drawBackgroundImmediately(DisplayEntity.TextDisplayEntity textDisplayEntity, DisplayEntity.TextDisplayEntity.Data data, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f, CallbackInfo ci) {
        if ((data.flags() & DisplayEntity.TextDisplayEntity.SEE_THROUGH_FLAG) != 0 && vertexConsumerProvider instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

}
