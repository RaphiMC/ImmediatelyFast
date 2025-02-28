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
package net.raphimc.immediatelyfast.neoforge.injection.mixins.hud_batching.compat.appleskin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.DrawContext;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "squeek.appleskin.client.HUDOverlayHandler", remap = false)
@Pseudo
public abstract class MixinAppleSkin_HUDOverlayHandler {

    @Inject(method = "drawExhaustionOverlay(FLnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"), remap = true)
    private static void forceDrawBatch(CallbackInfo ci, @Local(argsOnly = true) DrawContext drawContext) {
        if (ImmediatelyFast.runtimeConfig.hud_batching) {
            drawContext.draw();
        }
    }

}
