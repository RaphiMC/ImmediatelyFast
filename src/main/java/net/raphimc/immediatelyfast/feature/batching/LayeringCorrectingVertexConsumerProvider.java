package net.raphimc.immediatelyfast.feature.batching;

import net.minecraft.client.render.*;

public class LayeringCorrectingVertexConsumerProvider implements VertexConsumerProvider {

    private final VertexConsumerProvider delegate;

    public LayeringCorrectingVertexConsumerProvider(final VertexConsumerProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        return new LayeringCorrectingVertexConsumer(this.delegate.getBuffer(layer), layer.getDrawMode());
    }

}
