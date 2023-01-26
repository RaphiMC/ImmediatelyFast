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

import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = RenderLayer.class, priority = 500)
public abstract class MixinRenderLayer {

    @ModifyArg(method = {
            "method_34834" /*TEXT*/,
            "method_34833" /*TEXT_INTENSITY*/,
            "method_36437" /*TEXT_POLYGON_OFFSET*/,
            "method_36436" /*TEXT_INTENSITY_POLYGON_OFFSET*/,
            "method_37348" /*TEXT_SEE_THROUGH*/,
            "method_37347" /*TEXT_INTENSITY_SEE_THROUGH*/
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;of(Ljava/lang/String;Lnet/minecraft/client/render/VertexFormat;Lnet/minecraft/client/render/VertexFormat$DrawMode;IZZLnet/minecraft/client/render/RenderLayer$MultiPhaseParameters;)Lnet/minecraft/client/render/RenderLayer$MultiPhase;"), index = 5)
    private static boolean if$changeTranslucency(boolean value) {
        return false;
    }

}
