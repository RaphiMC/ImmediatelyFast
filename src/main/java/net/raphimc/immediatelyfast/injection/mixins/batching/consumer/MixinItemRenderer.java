package net.raphimc.immediatelyfast.injection.mixins.batching.consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ColorHelper;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import net.raphimc.immediatelyfast.feature.batching.BatchingRenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ItemRenderer.class, priority = 900)
public abstract class MixinItemRenderer {

    @ModifyArg(method = "renderGuiItemModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"))
    private VertexConsumerProvider renderItemIntoBuffer(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
        if (BatchingBuffers.LIT_ITEM_MODEL_CONSUMER != null || BatchingBuffers.UNLIT_ITEM_MODEL_CONSUMER != null) {
            // Get the model view transformations and apply them to the empty matrix stack.
            // When rendering that batch the model view matrix will be set to the identity matrix to not apply the model view transformations twice.
            matrices.peek().getPositionMatrix().set(RenderSystem.getModelViewMatrix());

            return model.isSideLit() ? BatchingBuffers.LIT_ITEM_MODEL_CONSUMER : BatchingBuffers.UNLIT_ITEM_MODEL_CONSUMER;
        }

        return vertexConsumers;
    }

    @ModifyArg(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I"))
    private VertexConsumerProvider renderTextInfoBuffer(VertexConsumerProvider vertexConsumers) {
        return BatchingBuffers.ITEM_OVERLAY_CONSUMER != null ? BatchingBuffers.ITEM_OVERLAY_CONSUMER : vertexConsumers;
    }

    /**
     * @author RK_01
     * @reason Allow batching of the vertex data. Overwritten for performance. Also fixes incompatibility with Colormatic caused by redirecting this method call.
     */
    @Overwrite
    public void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        if (BatchingBuffers.ITEM_OVERLAY_CONSUMER != null) {
            int color = alpha << 24 | red << 16 | green << 8 | blue;
            final float[] shaderColor = RenderSystem.getShaderColor();
            final int argb = (int) (shaderColor[3] * 255) << 24 | (int) (shaderColor[0] * 255) << 16 | (int) (shaderColor[1] * 255) << 8 | (int) (shaderColor[2] * 255);
            color = ColorHelper.Argb.mixColor(color, argb);
            final VertexConsumer vertexConsumer = BatchingBuffers.ITEM_OVERLAY_CONSUMER.getBuffer(BatchingRenderLayers.GUI_QUAD);
            vertexConsumer.vertex(x, y, 0).color(color).next();
            vertexConsumer.vertex(x, y + height, 0).color(color).next();
            vertexConsumer.vertex(x + width, y + height, 0).color(color).next();
            vertexConsumer.vertex(x + width, y, 0).color(color).next();
        } else {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buffer.vertex(x, y, 0).color(red, green, blue, alpha).next();
            buffer.vertex(x, y + height, 0).color(red, green, blue, alpha).next();
            buffer.vertex(x + width, y + height, 0).color(red, green, blue, alpha).next();
            buffer.vertex(x + width, y, 0).color(red, green, blue, alpha).next();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
    }

}
