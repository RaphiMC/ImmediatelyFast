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

import com.mojang.blaze3d.opengl.GlStateManager;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public abstract class MixinGlStateManager {

    @Inject(method = "_glBindFramebuffer", at = @At("HEAD"), cancellable = true)
    private static void lockFramebuffer(CallbackInfo ci) {
        if (ImmediatelyFast.signTextCache != null && ImmediatelyFast.signTextCache.lockFramebuffer) {
            ci.cancel();
        }
    }

    @Inject(method = "_viewport", at = @At("HEAD"), cancellable = true)
    private static void lockViewport(CallbackInfo ci) {
        if (ImmediatelyFast.signTextCache != null && ImmediatelyFast.signTextCache.lockViewport) {
            ci.cancel();
        }
    }

}
