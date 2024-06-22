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
package net.raphimc.immediatelyfast.forge.injection.mixins.screen_batching;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import net.raphimc.immediatelyfast.injection.processors.InjectAboveEverything;
import net.raphimc.immediatelyfast.injection.processors.InjectOnAllReturns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HandledScreen.class, priority = 1500)
public abstract class MixinHandledScreen {

    @InjectAboveEverything
    @Inject(method = "renderSlotHighlight", at = @At("HEAD"))
    private static void beginDrawSlotHighlightOnTop(CallbackInfo ci) {
        BatchingBuffers.beginItemOverlayRendering();
    }

    @InjectOnAllReturns
    @Inject(method = "renderSlotHighlight", at = @At("RETURN"))
    private static void endDrawSlotHighlightOnTop(CallbackInfo ci) {
        BatchingBuffers.endItemOverlayRendering();
    }

}
