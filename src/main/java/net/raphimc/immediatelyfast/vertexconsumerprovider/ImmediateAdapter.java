package net.raphimc.immediatelyfast.vertexconsumerprovider;

import com.google.common.collect.*;
import net.minecraft.client.render.*;
import net.raphimc.immediatelyfast.util.BufferBuilderPool;

import java.util.*;

public abstract class ImmediateAdapter extends VertexConsumerProvider.Immediate implements AutoCloseable {

    protected final Multimap<RenderLayer, BufferBuilder> fallbackBuffers = LinkedListMultimap.create();
    protected final Set<RenderLayer> activeLayers = new LinkedHashSet<>();

    public ImmediateAdapter() {
        this(ImmutableMap.of());
    }

    public ImmediateAdapter(final Map<RenderLayer, BufferBuilder> layerBuffers) {
        super(null, layerBuffers);
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        final BufferBuilder bufferBuilder = this.getOrCreateBufferBuilder(layer);
        if (bufferBuilder.isBuilding() && !layer.areVerticesNotShared()) {
            throw new IllegalStateException("Tried to write shared vertices into the same buffer");
        }

        if (!bufferBuilder.isBuilding()) {
            bufferBuilder.begin(layer.getDrawMode(), layer.getVertexFormat());
            this.activeLayers.add(layer);
        }
        return bufferBuilder;
    }

    @Override
    public void drawCurrentLayer() {
        for (RenderLayer layer : new LinkedHashSet<>(this.activeLayers)) {
            if (!this.layerBuffers.containsKey(layer)) this.draw(layer);
        }
    }

    @Override
    public void draw() {
        this.drawCurrentLayer();
        for (RenderLayer layer : this.layerBuffers.keySet()) {
            this.draw(layer);
        }
    }

    @Override
    public void draw(final RenderLayer layer) {
        this.activeLayers.remove(layer);
        this._draw(layer);
        this.fallbackBuffers.removeAll(layer);
    }

    @Override
    public void close() {
        this.activeLayers.clear();
        for (BufferBuilder bufferBuilder : this.fallbackBuffers.values()) {
            if (bufferBuilder.isBuilding()) {
                bufferBuilder.end().release();
            }
        }
        this.fallbackBuffers.clear();
    }

    protected abstract void _draw(RenderLayer layer);

    protected BufferBuilder getOrCreateBufferBuilder(final RenderLayer layer) {
        if (!layer.areVerticesNotShared()) {
            final BufferBuilder bufferBuilder = BufferBuilderPool.get();
            this.fallbackBuffers.put(layer, bufferBuilder);
            return bufferBuilder;
        } else if (this.layerBuffers.containsKey(layer)) {
            return this.layerBuffers.get(layer);
        } else if (this.fallbackBuffers.containsKey(layer)) {
            return this.fallbackBuffers.get(layer).iterator().next();
        } else {
            final BufferBuilder bufferBuilder = BufferBuilderPool.get();
            this.fallbackBuffers.put(layer, bufferBuilder);
            return bufferBuilder;
        }
    }

    protected Collection<BufferBuilder> getBufferBuilder(final RenderLayer layer) {
        if (this.fallbackBuffers.containsKey(layer)) {
            return this.fallbackBuffers.get(layer);
        } else if (this.layerBuffers.containsKey(layer)) {
            return Collections.singleton(this.layerBuffers.get(layer));
        } else {
            return Collections.emptyList();
        }
    }

}
