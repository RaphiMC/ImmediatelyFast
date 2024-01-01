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
package net.raphimc.immediatelyfast.forge.injection.mixins.hud_batching;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(ForgeGui.class)
public abstract class MixinForgeGui {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;forEach(Ljava/util/function/Consumer;)V", remap = false))
    private void batching(final ImmutableList<NamedGuiOverlay> instance, final Consumer<NamedGuiOverlay> consumer) {
        if (ImmediatelyFast.runtimeConfig.hud_batching) {
            instance.forEach(overlay -> {
                if (overlay.id().getNamespace().equals("minecraft")) {
                    if (VanillaGuiOverlay.DEBUG_TEXT.type().equals(overlay)) {
                        BatchingBuffers.beginDebugHudBatching();
                        consumer.accept(overlay);
                        BatchingBuffers.endDebugHudBatching();
                    } else {
                        BatchingBuffers.beginHudBatching();
                        consumer.accept(overlay);
                        BatchingBuffers.endHudBatching();
                    }
                } else {
                    consumer.accept(overlay);
                }
            });
        } else {
            instance.forEach(consumer);
        }
    }

}
