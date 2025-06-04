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
package net.raphimc.immediatelyfast.injection.mixins.fix_slow_buffer_upload_on_apple_gpu;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.gl.BufferManager;
import net.minecraft.client.gl.GlCommandEncoder;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteBuffer;

@Mixin(GlCommandEncoder.class)
public abstract class MixinGlCommandEncoder {

    @Redirect(method = "writeToBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/BufferManager;setBufferSubData(IILjava/nio/ByteBuffer;)V"))
    private void fixSlowBufferUploadOnAppleGpu(BufferManager instance, int buffer, int offset, ByteBuffer data, @Local(argsOnly = true) GpuBufferSlice gpuBufferSlice) {
        if (ImmediatelyFast.runtimeConfig.disable_fast_buffer_upload && offset == 0 && instance instanceof BufferManager.DefaultBufferManager) {
            instance.setBufferData(buffer, data, gpuBufferSlice.buffer().usage());
        } else {
            instance.setBufferSubData(buffer, offset, data);
        }
    }

}
