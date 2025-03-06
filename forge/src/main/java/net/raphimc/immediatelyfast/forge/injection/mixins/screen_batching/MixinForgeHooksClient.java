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
package net.raphimc.immediatelyfast.forge.injection.mixins.screen_batching;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.client.ForgeHooksClient;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ForgeHooksClient.class)
public abstract class MixinForgeHooksClient {

    @WrapOperation(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreenInternal(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/gui/DrawContext;IIF)V"))
    private static void screenBatching(Screen screen, DrawContext guiGraphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        final boolean batchScreen = screen instanceof ChatScreen;

        if (ImmediatelyFast.runtimeConfig.experimental_screen_batching && batchScreen) {
            BatchingBuffers.runBatched(guiGraphics, () -> original.call(screen, guiGraphics, mouseX, mouseY, partialTick));
        } else {
            original.call(screen, guiGraphics, mouseX, mouseY, partialTick);
        }
    }

}
