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
package net.raphimc.immediatelyfast.injection.mixins.core.compat;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.Drawer.class)
public abstract class MixinTextRenderer_Drawer {

    @Shadow
    @Final
    @Mutable
    private Matrix4f matrix;

    /**
     * Fixes <a href="https://github.com/RaphiMC/ImmediatelyFast/issues/81">https://github.com/RaphiMC/ImmediatelyFast/issues/81</a>
     */
    @Inject(method = "accept", at = @At(value = "RETURN"))
    private void fixNegativeAdvanceGlyphs(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir, @Local Glyph glyph) {
        final float advance = glyph.getAdvance(style.isBold());
        if (advance < 0) {
            this.matrix = this.matrix.copy();
            this.matrix.addToLastColumn(new Vec3f(0F, 0F, 0.03F));
        }
    }

}
