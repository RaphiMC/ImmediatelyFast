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
package net.raphimc.immediatelyfast.neoforge.injection.mixins.screen_batching;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = HandledScreen.class, priority = 500)
public abstract class MixinHandledScreen {

    @Shadow
    protected abstract void renderSlotHighlight(DrawContext guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick);

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;renderSlotHighlight(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;IIF)V"))
    private void drawSlotHightlightOnTop(HandledScreen<?> instance, DrawContext drawContext, Slot slot, int mouseX, int mouseY, float partialTick) {
        BatchingBuffers.beginItemOverlayRendering();
        this.renderSlotHighlight(drawContext, slot, mouseX, mouseY, partialTick);
        BatchingBuffers.endItemOverlayRendering();
    }

}
