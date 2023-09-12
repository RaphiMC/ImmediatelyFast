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
package net.raphimc.immediatelyfast.forge.injection.mixins.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraftforge.client.ForgeRenderTypes$Internal", priority = 500)
public abstract class MixinForgeRenderTypes {

    @ModifyArg(method = {
            "getText",
            "getTextIntensity",
            "getTextPolygonOffset",
            "getTextIntensityPolygonOffset",
            "getTextSeeThrough",
            "getTextIntensitySeeThrough"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeRenderTypes$Internal;of(Ljava/lang/String;Lnet/minecraft/client/render/VertexFormat;Lnet/minecraft/client/render/VertexFormat$DrawMode;IZZLnet/minecraft/client/render/RenderLayer$MultiPhaseParameters;)Lnet/minecraft/client/render/RenderLayer$MultiPhase;"), index = 5)
    private static boolean changeTranslucency(boolean value) {
        return false;
    }

}
