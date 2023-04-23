package net.raphimc.immediatelyfast.injection.mixins.fast_text_lookup;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextRenderer.Drawer.class)
public abstract class MixinTextRenderer_Drawer {

    @Unique
    private RenderLayer lastRenderLayer;

    @Unique
    private VertexConsumer lastVertexConsumer;

    @Unique
    private Identifier lastFont;

    @Unique
    private FontStorage lastFontStorage;

    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"))
    private VertexConsumer reduceGetBufferCalls(VertexConsumerProvider instance, RenderLayer renderLayer) {
        if (this.lastRenderLayer == renderLayer) {
            return this.lastVertexConsumer;
        }

        this.lastRenderLayer = renderLayer;
        return this.lastVertexConsumer = instance.getBuffer(renderLayer);
    }

    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getFontStorage(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/font/FontStorage;"))
    private FontStorage reduceGetFontStorageCalls(TextRenderer instance, Identifier id) {
        if (this.lastFont == id) {
            return this.lastFontStorage;
        }

        this.lastFont = id;
        return this.lastFontStorage = instance.getFontStorage(id);
    }

}
