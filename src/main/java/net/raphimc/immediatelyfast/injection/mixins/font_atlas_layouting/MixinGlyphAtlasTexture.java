package net.raphimc.immediatelyfast.injection.mixins.font_atlas_layouting;

import net.minecraft.client.font.GlyphAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Modifies the size of the glyph atlas texture to 2048x2048.
 * <p>
 * Vanilla uses a 256x256 texture, which is too small for high resolution fonts.
 * If the texture is too small, there may only be under ten glyphs per texture which causes a lot of texture switching when rendering text.
 */
@Mixin(GlyphAtlasTexture.class)
public abstract class MixinGlyphAtlasTexture {

    @ModifyConstant(method = "*", constant = @Constant(intValue = 256))
    private int modifyGlyphAtlasTextureSize(int original) {
        return 2048;
    }

    @ModifyConstant(method = "*", constant = @Constant(floatValue = 256F))
    private float modifyGlyphAtlasTextureSize(float original) {
        return 2048F;
    }

}
