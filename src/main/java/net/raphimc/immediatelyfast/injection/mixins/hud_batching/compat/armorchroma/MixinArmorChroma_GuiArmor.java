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
package net.raphimc.immediatelyfast.injection.mixins.hud_batching.compat.armorchroma;

import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "nukeduck.armorchroma.GuiArmor", remap = false)
@Pseudo
public abstract class MixinArmorChroma_GuiArmor {

    @Unique
    private boolean wasHudBatching;

    @Inject(method = "draw", at = @At("HEAD"))
    private void endHudBatching(CallbackInfo ci) {
        if (ImmediatelyFast.runtimeConfig.hud_batching && BatchingBuffers.isTextureBatching()) {
            BatchingBuffers.endHudBatching();
            this.wasHudBatching = true;
        }
    }

    @Inject(method = "draw", at = @At("RETURN"))
    private void beginHudBatching(CallbackInfo ci) {
        if (this.wasHudBatching) {
            BatchingBuffers.beginHudBatching();
            this.wasHudBatching = false;
        }
    }

}
