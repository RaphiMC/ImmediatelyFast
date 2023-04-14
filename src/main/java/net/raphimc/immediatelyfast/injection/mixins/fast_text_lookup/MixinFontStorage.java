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
    private final Glyph[] fastGlyphCache = new Glyph[65536];

    @Unique
    private final GlyphRenderer[] fastGlyphRendererCache = new GlyphRenderer[65536];

    @Inject(method = "setFonts", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;clear()V", ordinal = 0, remap = false))
    private void if$clearArrayCaches(List<Font> fonts, CallbackInfo ci) {
        Arrays.fill(this.fastGlyphCache, null);
        Arrays.fill(this.fastGlyphRendererCache, null);
    }

    @Inject(method = "getGlyph", at = @At("HEAD"), cancellable = true)
    private void if$fastGlyphCache(int codePoint, boolean validateAdvance, CallbackInfoReturnable<Glyph> cir) {
        if (codePoint >= 0 && codePoint < this.fastGlyphCache.length) {
            Glyph glyph = this.fastGlyphCache[codePoint];
            if (glyph == null) {
                glyph = this.fastGlyphCache[codePoint] = this.findGlyph(codePoint).getGlyph(validateAdvance);
            }

            cir.setReturnValue(glyph);
        }
    }

    @Inject(method = "getGlyphRenderer(I)Lnet/minecraft/client/font/GlyphRenderer;", at = @At("HEAD"), cancellable = true)
    private void if$fastGlyphRendererCache(int codePoint, CallbackInfoReturnable<GlyphRenderer> cir) {
        if (codePoint >= 0 && codePoint < this.fastGlyphRendererCache.length) {
            GlyphRenderer glyphRenderer = this.fastGlyphRendererCache[codePoint];
            if (glyphRenderer == null) {
                glyphRenderer = this.fastGlyphRendererCache[codePoint] = this.findGlyphRenderer(codePoint);
            }

            cir.setReturnValue(glyphRenderer);
        }
    }

}
