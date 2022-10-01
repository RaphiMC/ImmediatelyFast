package net.raphimc.immediatelyfast.vertexconsumerprovider;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;

import java.util.Map;

public class BatchableImmediate extends ImmediateAdapter {

    public BatchableImmediate() {
    }

    public BatchableImmediate(final Map<RenderLayer, BufferBuilder> layerBuffers) {
        super(layerBuffers);
    }

    @Override
    protected void _draw(final RenderLayer layer) {
        for (BufferBuilder bufferBuilder : this.getBufferBuilder(layer)) {
            if (bufferBuilder != null) layer.draw(bufferBuilder, 0, 0, 0);
        }
    }

}
