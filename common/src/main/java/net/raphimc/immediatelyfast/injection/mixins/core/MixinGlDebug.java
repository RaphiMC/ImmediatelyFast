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
package net.raphimc.immediatelyfast.injection.mixins.core;

import net.minecraft.client.gl.GlDebug;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlDebug.class)
public abstract class MixinGlDebug {

    @Unique
    private static long immediatelyFast$lastTime;

    @ModifyVariable(method = "enableDebug", at = @At("HEAD"), index = 1, argsOnly = true)
    private static boolean enableSyncDebug(boolean sync) {
        final GLCapabilities capabilities = GL.getCapabilities();
        return sync || (ImmediatelyFast.config.debug_only_print_additional_error_information && (capabilities.GL_KHR_debug || capabilities.GL_ARB_debug_output));
    }

    @Redirect(method = "onDebugMessage", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    private void appendStackTrace(Logger instance, String message, Object argument) {
        if (ImmediatelyFast.config.debug_only_print_additional_error_information && System.currentTimeMillis() - immediatelyFast$lastTime > 1000) {
            immediatelyFast$lastTime = System.currentTimeMillis();
            instance.info(message, argument, new Exception());
        } else {
            instance.info(message, argument);
        }
    }

}
