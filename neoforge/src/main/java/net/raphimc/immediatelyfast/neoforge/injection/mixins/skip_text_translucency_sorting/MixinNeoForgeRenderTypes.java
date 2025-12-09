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
package net.raphimc.immediatelyfast.neoforge.injection.mixins.skip_text_translucency_sorting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.neoforged.neoforge.client.NeoForgeRenderTypes$Internal", priority = 500)
public abstract class MixinNeoForgeRenderTypes {

    @ModifyArg(method = {
            "getTextFiltered",
            "getTextIntensityFiltered",
            "getTextPolygonOffsetFiltered",
            "getTextIntensityPolygonOffsetFiltered",
            "getTextSeeThroughFiltered",
            "getTextIntensitySeeThroughFiltered"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;create(Ljava/lang/String;IZZLcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/renderer/RenderType$CompositeState;)Lnet/minecraft/client/renderer/RenderType$CompositeRenderType;"), index = 3)
    private static boolean changeTranslucency(boolean value) {
        return false;
    }

}
