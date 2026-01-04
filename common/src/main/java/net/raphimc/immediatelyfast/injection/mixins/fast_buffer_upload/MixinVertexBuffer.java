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
package net.raphimc.immediatelyfast.injection.mixins.fast_buffer_upload;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gl.GlUsage;
import net.minecraft.client.gl.GpuBuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VertexBuffer.class)
public abstract class MixinVertexBuffer {

    @Shadow
    @Final
    private GlUsage usage;

    @WrapWithCondition(method = "uploadVertexBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GpuBuffer;resize(I)V"))
    private boolean onlyResizeIfNeeded(GpuBuffer instance, int newSize) {
        return !ImmediatelyFast.runtimeConfig.fast_buffer_upload || this.usage == GlUsage.STATIC_WRITE || newSize > instance.size;
    }

}
