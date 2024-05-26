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
package net.raphimc.immediatelyfast.injection.mixins.hud_batching;

import net.minecraft.client.gl.VertexBuffer;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffer;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
public abstract class MixinVertexBuffer {

    @Shadow
    public abstract void bind();

    @Unique
    private static boolean immediatelyFast$isForceDrawing;

    @Inject(method = "drawInternal", at = @At("HEAD"))
    private void checkForDrawCallWhileBatching(CallbackInfo ci) {
        // Force draw the current batch if
        // we are not already force drawing (prevent recursion)
        // and the buffer being drawn is not one of the IF batching buffers
        // and we are currently batching (just checks if one of the vertex consumers is set)
        // and there is data to draw
        if (!immediatelyFast$isForceDrawing && !BatchingBuffer.IS_DRAWING && BatchingBuffers.FILL_CONSUMER != null && BatchingBuffers.hasDataToDraw()) {
            // If some mod tries to directly draw something while we are batching, we should end the current batch and start a new one, so that the draw order is correct.
            immediatelyFast$isForceDrawing = true;
            BatchingBuffers.forceDrawBuffers();
            this.bind();
            immediatelyFast$isForceDrawing = false;
        }
    }

}
