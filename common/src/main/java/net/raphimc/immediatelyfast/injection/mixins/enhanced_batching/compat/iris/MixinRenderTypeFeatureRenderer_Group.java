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
package net.raphimc.immediatelyfast.injection.mixins.enhanced_batching.compat.iris;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.raphimc.immediatelyfast.injection.interfaces.IIris_OuterWrappedRenderType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer$Group")
public abstract class MixinRenderTypeFeatureRenderer_Group {

    // https://github.com/RaphiMC/ImmediatelyFast/issues/582
    @ModifyExpressionValue(method = "getOrAddDraw", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/feature/RenderTypeFeatureRenderer$Group;canReorder:Z", opcode = Opcodes.GETFIELD))
    private boolean fixOrder(final boolean original, @Local(name = "renderType", argsOnly = true) final RenderType renderType) {
        if (immediatelyFast$isBannerPattern(renderType)) {
            return false;
        } else if (renderType instanceof IIris_OuterWrappedRenderType wrappedRenderType && immediatelyFast$isBannerPattern(wrappedRenderType.immediatelyFast$getWrapped())) {
            return false;
        } else {
            return original;
        }
    }

    @Unique
    private static boolean immediatelyFast$isBannerPattern(final RenderType renderType) {
        return renderType.state.textures.get("Sampler0") instanceof RenderSetup.TextureBinding textureBinding && textureBinding.location().equals(Sheets.BANNER_SHEET);
    }

}
