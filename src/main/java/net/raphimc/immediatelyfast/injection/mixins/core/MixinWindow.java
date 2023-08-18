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
package net.raphimc.immediatelyfast.injection.mixins.core;

import net.minecraft.client.util.Window;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.fast_buffer_upload.PersistentMappedStreamingBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GLCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class MixinWindow {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initImmediatelyFast(CallbackInfo ci) {
        final GLCapabilities cap = GL.getCapabilities();
        final String gpuVendor = GL11C.glGetString(GL11C.GL_VENDOR);
        final String gpuModel = GL11C.glGetString(GL11C.GL_RENDERER);
        final String glVersion = GL11C.glGetString(GL11C.GL_VERSION);
        ImmediatelyFast.LOGGER.info("Initializing IF on " + gpuModel + " (" + gpuVendor + ") with OpenGL " + glVersion);

        boolean isNvidia = false;
        boolean isAmd = false;
        boolean isIntel = false;
        if (gpuVendor != null) {
            final String gpuVendorLower = gpuVendor.toLowerCase();

            isNvidia = gpuVendorLower.startsWith("nvidia");
            isAmd = gpuVendorLower.startsWith("ati") || gpuVendorLower.startsWith("amd");
            isIntel = gpuVendorLower.startsWith("intel");
        }

        if (ImmediatelyFast.config.fast_buffer_upload) {
            if (cap.GL_ARB_direct_state_access && cap.GL_ARB_buffer_storage && cap.glMemoryBarrier != 0) {
                if (isAmd && !ImmediatelyFast.config.debug_only_and_not_recommended_disable_hardware_conflict_handling) {
                    // Explicit flush causes AMD GPUs to stall the pipeline a lot.
                    ImmediatelyFast.LOGGER.warn("AMD GPU detected. Enabling coherent buffer mapping.");
                    ImmediatelyFast.config.fast_buffer_upload_explicit_flush = false;
                }

                ImmediatelyFast.persistentMappedStreamingBuffer = new PersistentMappedStreamingBuffer(ImmediatelyFast.config.fast_buffer_upload_size_mb * 1024 * 1024);
            } else {
                ImmediatelyFast.LOGGER.warn("Your GPU doesn't support ARB_direct_state_access and/or ARB_buffer_storage and/or glMemoryBarrier. Falling back to legacy fast buffer upload method.");
                if (!isNvidia && !ImmediatelyFast.config.debug_only_and_not_recommended_disable_hardware_conflict_handling) {
                    // Legacy fast buffer upload causes a lot of graphical issues on non NVIDIA GPUs.
                    ImmediatelyFast.LOGGER.warn("Non NVIDIA GPU detected. Force disabling fast buffer upload optimization.");
                } else {
                    ImmediatelyFast.runtimeConfig.legacy_fast_buffer_upload = true;
                }
            }
        }
    }

}
