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
package net.raphimc.immediatelyfast.injection.mixins.hud_batching;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DebugHud.class, priority = 500)
public abstract class MixinDebugHud {

    @WrapOperation(method = "render", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;renderLeftText(Lnet/minecraft/client/util/math/MatrixStack;)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;renderRightText(Lnet/minecraft/client/util/math/MatrixStack;)V"),
    })
    private void if$batching(final DebugHud instance, final MatrixStack matrices, final Operation<Void> operation) {
        BatchingBuffers.beginHudBatching();
        operation.call(instance, matrices);
        BatchingBuffers.endHudBatching();
    }

}
