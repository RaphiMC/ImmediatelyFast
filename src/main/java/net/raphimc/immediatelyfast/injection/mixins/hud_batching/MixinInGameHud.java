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
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.JumpingMount;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(value = InGameHud.class, priority = 500)
public abstract class MixinInGameHud {

    @WrapOperation(method = "render", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusBars(Lnet/minecraft/client/util/math/MatrixStack;)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMountHealth(Lnet/minecraft/client/util/math/MatrixStack;)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/util/math/MatrixStack;)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderCrosshair(Lnet/minecraft/client/util/math/MatrixStack;)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/BossBarHud;render(Lnet/minecraft/client/util/math/MatrixStack;)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SubtitlesHud;render(Lnet/minecraft/client/util/math/MatrixStack;)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SpectatorHud;render(Lnet/minecraft/client/util/math/MatrixStack;)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SpectatorHud;renderSpectatorMenu(Lnet/minecraft/client/util/math/MatrixStack;)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHeldItemTooltip(Lnet/minecraft/client/util/math/MatrixStack;)V"),
    })
    private void if$Batching(@Coerce final Object instance, final MatrixStack matrices, final Operation<Void> operation) {
        if (ImmediatelyFast.runtimeConfig.hud_batching) {
            BatchingBuffers.beginHudBatching();
            operation.call(instance, matrices);
            BatchingBuffers.endHudBatching();
        } else {
            operation.call(instance, matrices);
        }
    }

    @WrapOperation(method = "render", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/util/math/MatrixStack;III)V"),
    })
    private void if$Batching(@Coerce final Object instance, final MatrixStack matrices, final int currentTick, final int mouseX, final int mouseY, final Operation<Void> operation) {
        if (ImmediatelyFast.runtimeConfig.hud_batching) {
            BatchingBuffers.beginHudBatching();
            operation.call(instance, matrices, currentTick, mouseX, mouseY);
            BatchingBuffers.endHudBatching();
        } else {
            operation.call(instance, matrices, currentTick, mouseX, mouseY);
        }
    }

    @WrapOperation(method = "render", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;render(Lnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"),
    })
    private void if$Batching(@Coerce final Object instance, final MatrixStack matrices, final int scaledWindowWidth, final Scoreboard scoreboard, final ScoreboardObjective objective, final Operation<Void> operation) {
        if (ImmediatelyFast.runtimeConfig.hud_batching) {
            BatchingBuffers.beginHudBatching();
            operation.call(instance, matrices, scaledWindowWidth, scoreboard, objective);
            BatchingBuffers.endHudBatching();
        } else {
            operation.call(instance, matrices, scaledWindowWidth, scoreboard, objective);
        }
    }

    @WrapOperation(method = "render", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMountJumpBar(Lnet/minecraft/entity/JumpingMount;Lnet/minecraft/client/util/math/MatrixStack;I)V"),
    })
    private void if$Batching(@Coerce final Object instance, final JumpingMount mount, final MatrixStack matrices, final int x, final Operation<Void> operation) {
        if (ImmediatelyFast.runtimeConfig.hud_batching) {
            BatchingBuffers.beginHudBatching();
            operation.call(instance, mount, matrices, x);
            BatchingBuffers.endHudBatching();
        } else {
            operation.call(instance, mount, matrices, x);
        }
    }

    @WrapOperation(method = "render", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"),
    })
    private void if$Batching(@Coerce final Object instance, final MatrixStack matrices, final ScoreboardObjective objective, final Operation<Void> operation) {
        if (ImmediatelyFast.runtimeConfig.hud_batching) {
            BatchingBuffers.beginHudBatching();
            operation.call(instance, matrices, objective);
            BatchingBuffers.endHudBatching();
        } else {
            operation.call(instance, matrices, objective);
        }
    }

    @WrapOperation(method = "render", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(FLnet/minecraft/client/util/math/MatrixStack;)V"),
    })
    private void if$Batching(@Coerce final Object instance, final float tickDelta, final MatrixStack matrices, final Operation<Void> operation) {
        if (ImmediatelyFast.config.experimental_item_hud_batching && ImmediatelyFast.runtimeConfig.hud_batching) {
            BatchingBuffers.beginHudBatching();
            BatchingBuffers.beginItemBatching();
            operation.call(instance, tickDelta, matrices);
            BatchingBuffers.endHudBatching();
            BatchingBuffers.endItemBatching();
        } else {
            operation.call(instance, tickDelta, matrices);
        }
    }

    @WrapOperation(method = "render", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderExperienceBar(Lnet/minecraft/client/util/math/MatrixStack;I)V"),
    })
    private void if$Batching(@Coerce final Object instance, final MatrixStack matrices, final int x, final Operation<Void> operation) {
        if (ImmediatelyFast.runtimeConfig.hud_batching) {
            BatchingBuffers.beginHudBatching();
            operation.call(instance, matrices, x);
            BatchingBuffers.endHudBatching();
        } else {
            operation.call(instance, matrices, x);
        }
    }

}
