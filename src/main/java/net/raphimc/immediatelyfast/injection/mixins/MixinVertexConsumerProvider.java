package net.raphimc.immediatelyfast.injection.mixins;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.render.*;
import net.raphimc.immediatelyfast.injection.interfaces.IBufferBuilder;
import net.raphimc.immediatelyfast.vertexconsumerprovider.BatchableImmediate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Map;

@Mixin(VertexConsumerProvider.class)
public interface MixinVertexConsumerProvider {

    /**
     * @author RK_01
     * @reason Universal Batching
     */
    @Overwrite
    static VertexConsumerProvider.Immediate immediate(BufferBuilder buffer) {
        return immediate(ImmutableMap.of(), buffer);
    }

    /**
     * @author RK_01
     * @reason Universal Batching
     */
    @Overwrite
    static VertexConsumerProvider.Immediate immediate(Map<RenderLayer, BufferBuilder> layerBuffers, BufferBuilder fallbackBuffer) {
        if (!fallbackBuffer.equals(Tessellator.getInstance().getBuffer())) {
            ((IBufferBuilder) fallbackBuffer).release();
        }
        return new BatchableImmediate(layerBuffers);
    }

}
