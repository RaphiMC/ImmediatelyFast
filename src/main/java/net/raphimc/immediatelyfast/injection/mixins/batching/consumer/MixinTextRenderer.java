package net.raphimc.immediatelyfast.injection.mixins.batching.consumer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;

@Mixin(value = TextRenderer.class, priority = 900)
public abstract class MixinTextRenderer {

    @Shadow
    public abstract int draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light, boolean rightToLeft);

    @Shadow
    public abstract int draw(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light);

    /**
     * @author RK_01
     * @reason Allow batching of the vertex data. Overwritten for performance.
     */
    @Overwrite
    private int draw(String text, float x, float y, int color, Matrix4f matrix, boolean shadow, boolean mirror) {
        if (text == null) {
            return 0;
        } else if (BatchingBuffers.TEXT_CONSUMER != null) {
            return this.draw(text, x, y, color, shadow, matrix, BatchingBuffers.TEXT_CONSUMER, false, 0, 15728880, mirror);
        } else {
            final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            final int width = this.draw(text, x, y, color, shadow, matrix, immediate, false, 0, 15728880, mirror);
            immediate.draw();
            return width;
        }
    }

    /**
     * @author RK_01
     * @reason Allow batching of the vertex data. Overwritten for performance.
     */
    @Overwrite
    private int draw(OrderedText text, float x, float y, int color, Matrix4f matrix, boolean shadow) {
        if (BatchingBuffers.TEXT_CONSUMER != null) {
            return this.draw(text, x, y, color, shadow, matrix, BatchingBuffers.TEXT_CONSUMER, false, 0, 15728880);
        } else {
            final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            final int width = this.draw(text, x, y, color, shadow, matrix, immediate, false, 0, 15728880);
            immediate.draw();
            return width;
        }
    }

}
