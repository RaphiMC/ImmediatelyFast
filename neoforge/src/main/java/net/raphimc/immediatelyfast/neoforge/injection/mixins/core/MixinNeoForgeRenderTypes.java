/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.immediatelyfast.neoforge.injection.mixins.core;

import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.neoforged.neoforge.client.NeoForgeRenderTypes$Internal", priority = 500)
public abstract class MixinNeoForgeRenderTypes {

    @Inject(method = {
            "getText",
            "getTextIntensity",
            "getTextPolygonOffset",
            "getTextIntensityPolygonOffset",
            "getTextSeeThrough",
            "getTextIntensitySeeThrough"
    }, at = @At(value = "RETURN"), remap = false) // Forge doesn't allow me to target the of() call for some reason
    private static void changeTranslucency(CallbackInfoReturnable<RenderLayer> cir) {
        cir.getReturnValue().translucent = false;
    }

}
