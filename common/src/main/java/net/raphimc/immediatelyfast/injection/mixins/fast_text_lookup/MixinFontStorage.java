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
package net.raphimc.immediatelyfast.injection.mixins.fast_text_lookup;

import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;

@Mixin(FontStorage.class)
public abstract class MixinFontStorage {

    @Shadow
    protected abstract FontStorage.GlyphPair findGlyph(int codePoint);

    @Shadow
    protected abstract GlyphRenderer findGlyphRenderer(int codePoint);

    @Unique
    private final Glyph[] immediatelyFast$fastGlyphCache = new Glyph[65536];

    @Unique
    private final GlyphRenderer[] immediatelyFast$fastGlyphRendererCache = new GlyphRenderer[65536];

    @Inject(method = "setFonts", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;clear()V", ordinal = 0, remap = false))
    private void clearArrayCaches(List<Font> fonts, CallbackInfo ci) {
        Arrays.fill(this.immediatelyFast$fastGlyphCache, null);
        Arrays.fill(this.immediatelyFast$fastGlyphRendererCache, null);
    }

    @Inject(method = "getGlyph", at = @At("HEAD"), cancellable = true)
    private void fastGlyphCache(int codePoint, boolean validateAdvance, CallbackInfoReturnable<Glyph> cir) {
        if (codePoint >= 0 && codePoint < this.immediatelyFast$fastGlyphCache.length) {
            Glyph glyph = this.immediatelyFast$fastGlyphCache[codePoint];
            if (glyph == null) {
                glyph = this.immediatelyFast$fastGlyphCache[codePoint] = this.findGlyph(codePoint).getGlyph(validateAdvance);
            }

            cir.setReturnValue(glyph);
        }
    }

    @Inject(method = "getGlyphRenderer(I)Lnet/minecraft/client/font/GlyphRenderer;", at = @At("HEAD"), cancellable = true)
    private void fastGlyphRendererCache(int codePoint, CallbackInfoReturnable<GlyphRenderer> cir) {
        if (codePoint >= 0 && codePoint < this.immediatelyFast$fastGlyphRendererCache.length) {
            GlyphRenderer glyphRenderer = this.immediatelyFast$fastGlyphRendererCache[codePoint];
            if (glyphRenderer == null) {
                glyphRenderer = this.immediatelyFast$fastGlyphRendererCache[codePoint] = this.findGlyphRenderer(codePoint);
            }

            cir.setReturnValue(glyphRenderer);
        }
    }

}
