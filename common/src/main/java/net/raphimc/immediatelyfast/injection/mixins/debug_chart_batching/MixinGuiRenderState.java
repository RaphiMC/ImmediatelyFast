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
package net.raphimc.immediatelyfast.injection.mixins.debug_chart_batching;

import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.raphimc.immediatelyfast.feature.debug_chart_batching.DebugChartBatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public abstract class MixinGuiRenderState {

    @Inject(method = "addGuiElement", at = @At("HEAD"), cancellable = true)
    private void collectChartRectangles(
            final GuiElementRenderState element,
            final CallbackInfo ci
    ) {
        if (DebugChartBatch.capture((GuiRenderState) (Object) this, element)) {
            ci.cancel();
        }
    }

    @Inject(
            method = {
                    "addText",
                    "addItem",
                    "addPicturesInPictureState",
                    "addBlitToCurrentLayer",
                    "addGlyphToCurrentLayer",
                    "nextStratum",
                    "blurBeforeThisStratum",
                    "up",
                    "reset"
            },
            at = @At("HEAD")
    )
    private void flushChartRectangles(final CallbackInfo ci) {
        DebugChartBatch.barrier();
    }

}
