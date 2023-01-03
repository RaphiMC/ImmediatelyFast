package net.raphimc.immediatelyfast.injection.mixins.hud_batching.consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.ColorHelper;
import net.raphimc.immediatelyfast.feature.batching.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DrawableHelper.class, priority = 1500)
public abstract class MixinDrawableHelper {

    @Inject(method = "fill(Lorg/joml/Matrix4f;IIIII)V", at = @At("HEAD"), cancellable = true)
    private static void fillIntoBuffer(Matrix4f matrix, int x1, int y1, int x2, int y2, int color, CallbackInfo ci) {
        if (BatchingBuffers.FILL_CONSUMER != null) {
            ci.cancel();
            if (x1 < x2) {
                x1 = x1 ^ x2;
                x2 = x1 ^ x2;
                x1 = x1 ^ x2;
            }
            if (y1 < y2) {
                y1 = y1 ^ y2;
                y2 = y1 ^ y2;
                y1 = y1 ^ y2;
            }
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int argb = (int) (shaderColor[3] * 255) << 24 | (int) (shaderColor[0] * 255) << 16 | (int) (shaderColor[1] * 255) << 8 | (int) (shaderColor[2] * 255);
            color = ColorHelper.Argb.mixColor(color, argb);
            final VertexConsumer vertexConsumer = BatchingBuffers.FILL_CONSUMER.getBuffer(BatchingRenderLayers.FILLED_QUAD.apply(BlendFuncDepthFunc.current()));
            vertexConsumer.vertex(matrix, x1, y2, 0F).color(color).next();
            vertexConsumer.vertex(matrix, x2, y2, 0F).color(color).next();
            vertexConsumer.vertex(matrix, x2, y1, 0F).color(color).next();
            vertexConsumer.vertex(matrix, x1, y1, 0F).color(color).next();
        }
    }

    @Inject(method = "drawTexturedQuad", at = @At("HEAD"), cancellable = true)
    private static void drawTexturedQuadIntoBuffer(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, CallbackInfo ci) {
        if (BatchingBuffers.TEXTURE_CONSUMER != null) {
            ci.cancel();
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int r = (int) (shaderColor[0] * 255);
            final int g = (int) (shaderColor[1] * 255);
            final int b = (int) (shaderColor[2] * 255);
            final int a = (int) (shaderColor[3] * 255);
            final VertexConsumer vertexConsumer = BatchingBuffers.TEXTURE_CONSUMER.getBuffer(BatchingRenderLayers.COLORED_TEXTURE.apply(RenderSystem.getShaderTexture(0), BlendFuncDepthFunc.current()));
            vertexConsumer.vertex(matrix, x0, y1, z).texture(u0, v1).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x1, y1, z).texture(u1, v1).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x1, y0, z).texture(u1, v0).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x0, y0, z).texture(u0, v0).color(r, g, b, a).next();
        }
    }

}
