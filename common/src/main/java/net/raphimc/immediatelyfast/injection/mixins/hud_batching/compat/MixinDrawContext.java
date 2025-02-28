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
package net.raphimc.immediatelyfast.injection.mixins.hud_batching.compat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.raphimc.immediatelyfast.feature.core.BatchableBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class MixinDrawContext {

    @Shadow
    public abstract void draw();

    @Shadow
    public VertexConsumerProvider.Immediate vertexConsumers;

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;getCooldownProgress(Lnet/minecraft/item/Item;F)F")), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(Lnet/minecraft/client/render/RenderLayer;IIIII)V"))
    private void forceDraw(CallbackInfo ci) {
        if (this.vertexConsumers instanceof BatchableBufferSource) {
            this.draw();
        }
    }

}
