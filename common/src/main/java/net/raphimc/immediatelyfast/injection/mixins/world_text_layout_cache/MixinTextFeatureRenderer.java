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
package net.raphimc.immediatelyfast.injection.mixins.world_text_layout_cache;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.raphimc.immediatelyfast.feature.world_text_layout_cache.WorldTextLayoutCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextFeatureRenderer.class)
public abstract class MixinTextFeatureRenderer {

    @Redirect(method = "renderText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;"))
    private static Font.PreparedText cacheTextLayout(final Font instance, final FormattedCharSequence text, final float x, final float y, final int color, final boolean dropShadow, final boolean includeEmpty, final int backgroundColor) {
        return WorldTextLayoutCache.prepareText(instance, text, x, y, color, dropShadow, backgroundColor);
    }

    @Redirect(method = "renderText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;prepare8xTextOutline(Lnet/minecraft/util/FormattedCharSequence;FFI)Lnet/minecraft/client/gui/Font$PreparedText;"))
    private static Font.PreparedText cacheTextOutlineLayout(final Font instance, final FormattedCharSequence text, final float x, final float y, final int outlineColor) {
        return WorldTextLayoutCache.prepare8xTextOutline(instance, text, x, y, outlineColor);
    }

}
