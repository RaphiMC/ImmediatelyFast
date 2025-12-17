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
package net.raphimc.immediatelyfast.injection.mixins.core.compat.iris;

import net.minecraft.client.render.BufferBuilderStorage;
import net.raphimc.immediatelyfast.feature.core.BatchableBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "net.irisshaders.iris.shadows.ShadowRenderer", remap = false)
@Pseudo
public abstract class MixinIris_ShadowRenderer {

    @Shadow
    @Final
    private BufferBuilderStorage buffers;

    @Inject(method = "renderShadows", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw()V"))
    private void clearDataFromModsWhichRenderIntoTheWrongBuffer(CallbackInfo ci) {
        // GeckoLib renders into the outline buffer during the shadow pass. This causes a memory leak because Iris never clears or renders it.
        if (this.buffers.getOutlineVertexConsumers().plainDrawer instanceof BatchableBufferSource batchableBufferSource) {
            batchableBufferSource.close();
        }
    }

}
