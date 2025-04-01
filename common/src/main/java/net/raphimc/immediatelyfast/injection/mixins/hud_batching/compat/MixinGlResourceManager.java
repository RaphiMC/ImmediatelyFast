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

import net.minecraft.client.gl.GlResourceManager;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import net.raphimc.immediatelyfast.injection.interfaces.IGlResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlResourceManager.class)
public abstract class MixinGlResourceManager implements IGlResourceManager {

    @Shadow
    public boolean renderPassOpen;

    @Unique
    private boolean immediatelyFast$skipRenderPassClose = false;

    @Inject(method = {"drawObjectsWithRenderPass", "drawBoundObjectWithRenderPass"}, at = @At("HEAD"))
    private void checkForDrawCallWhileBatching(CallbackInfo ci) {
        if (BatchingBuffers.isHudBatching()) {
            // If some mod tries to directly draw something while we are batching, we should end the current batch and start a new one, so that the draw order is correct.
            BatchingBuffers.tryForceDrawHudBuffers();
        }
    }

    @Inject(method = "closePass", at = @At("HEAD"), cancellable = true)
    private void skipRenderPassClose(CallbackInfo ci) {
        if (this.immediatelyFast$skipRenderPassClose) {
            this.renderPassOpen = false;
            ci.cancel();
        }
    }

    @Override
    public void immediatelyfast$skipRenderPassClose(final boolean skipRenderPassClose) {
        this.immediatelyFast$skipRenderPassClose = skipRenderPassClose;
    }

}
