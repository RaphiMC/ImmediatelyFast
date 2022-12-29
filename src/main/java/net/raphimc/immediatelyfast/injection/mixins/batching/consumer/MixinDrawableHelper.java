package net.raphimc.immediatelyfast.injection.mixins.batching.consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Matrix4f;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import net.raphimc.immediatelyfast.feature.batching.BatchingRenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = DrawableHelper.class, priority = 900)
public abstract class MixinDrawableHelper {

    /**
     * @author RK_01
     * @reason Allow batching of the vertex data. Overwritten for performance.
     */
    @Overwrite
    public static void fill(Matrix4f matrix, int x1, int y1, int x2, int y2, int color) {
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

        if (BatchingBuffers.FILL_CONSUMER != null) {
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int argb = (int) (shaderColor[3] * 255) << 24 | (int) (shaderColor[0] * 255) << 16 | (int) (shaderColor[1] * 255) << 8 | (int) (shaderColor[2] * 255);
            color = ColorHelper.Argb.mixColor(color, argb);
            final VertexConsumer vertexConsumer = BatchingBuffers.FILL_CONSUMER.getBuffer(BatchingRenderLayers.FILLED_QUAD);
            vertexConsumer.vertex(matrix, x1, y2, 0F).color(color).next();
            vertexConsumer.vertex(matrix, x2, y2, 0F).color(color).next();
            vertexConsumer.vertex(matrix, x2, y1, 0F).color(color).next();
            vertexConsumer.vertex(matrix, x1, y1, 0F).color(color).next();
        } else {
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            final BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix, x1, y2, 0F).color(color).next();
            bufferBuilder.vertex(matrix, x2, y2, 0F).color(color).next();
            bufferBuilder.vertex(matrix, x2, y1, 0F).color(color).next();
            bufferBuilder.vertex(matrix, x1, y1, 0F).color(color).next();
            BufferRenderer.drawWithShader(bufferBuilder.end());
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    /**
     * @author RK_01
     * @reason Allow batching of the vertex data. Overwritten for performance.
     */
    @Overwrite
    public static void drawTexturedQuad(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        if (BatchingBuffers.TEXTURE_CONSUMER != null) {
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int r = (int) (shaderColor[0] * 255);
            final int g = (int) (shaderColor[1] * 255);
            final int b = (int) (shaderColor[2] * 255);
            final int a = (int) (shaderColor[3] * 255);
            final VertexConsumer vertexConsumer = BatchingBuffers.TEXTURE_CONSUMER.getBuffer(BatchingRenderLayers.COLORED_TEXTURE.apply(RenderSystem.getShaderTexture(0)));
            vertexConsumer.vertex(matrix, x0, y1, z).texture(u0, v1).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x1, y1, z).texture(u1, v1).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x1, y0, z).texture(u1, v0).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, x0, y0, z).texture(u0, v0).color(r, g, b, a).next();
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            final BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            bufferBuilder.vertex(matrix, x0, y1, z).texture(u0, v1).next();
            bufferBuilder.vertex(matrix, x1, y1, z).texture(u1, v1).next();
            bufferBuilder.vertex(matrix, x1, y0, z).texture(u1, v0).next();
            bufferBuilder.vertex(matrix, x0, y0, z).texture(u0, v0).next();
            BufferRenderer.drawWithShader(bufferBuilder.end());
        }
    }

}
