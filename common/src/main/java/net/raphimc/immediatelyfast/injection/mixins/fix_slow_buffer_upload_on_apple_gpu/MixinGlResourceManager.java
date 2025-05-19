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
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gl.GlResourceManager;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteBuffer;

@Mixin(GlResourceManager.class)
public abstract class MixinGlResourceManager {

    @Redirect(method = "writeToBuffer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_glBufferSubData(IILjava/nio/ByteBuffer;)V"))
    private void fixSlowBufferUploadOnAppleGpu(int target, int offset, ByteBuffer data, @Local(argsOnly = true) GpuBuffer gpuBuffer) {
        if (ImmediatelyFast.runtimeConfig.disable_fast_buffer_upload && offset == 0) {
            GlStateManager._glBufferData(target, data, GlConst.toGl(gpuBuffer.usage()));
        } else {
            GlStateManager._glBufferSubData(target, offset, data);
        }
    }

}
