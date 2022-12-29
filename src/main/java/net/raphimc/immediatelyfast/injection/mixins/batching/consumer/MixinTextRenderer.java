package net.raphimc.immediatelyfast.injection.mixins.batching.consumer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = TextRenderer.class, priority = 1100)
public abstract class MixinTextRenderer {

    @ModifyArg(method = "draw(Ljava/lang/String;FFILorg/joml/Matrix4f;ZZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZIIZ)I"))
    private VertexConsumerProvider renderTextIntoBuffer1(VertexConsumerProvider vertexConsumers) {
        return BatchingBuffers.TEXT_CONSUMER != null ? BatchingBuffers.TEXT_CONSUMER : vertexConsumers;
    }

    @ModifyArg(method = "draw(Lnet/minecraft/text/OrderedText;FFILorg/joml/Matrix4f;Z)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I"))
    private VertexConsumerProvider renderTextIntoBuffer2(VertexConsumerProvider vertexConsumers) {
        return BatchingBuffers.TEXT_CONSUMER != null ? BatchingBuffers.TEXT_CONSUMER : vertexConsumers;
    }

}
