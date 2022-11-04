package net.raphimc.immediatelyfast.vertexconsumerprovider;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.render.*;
import net.raphimc.immediatelyfast.util.BufferBuilderPool;

import java.util.*;

public abstract class ImmediateAdapter extends VertexConsumerProvider.Immediate implements AutoCloseable {

    /**
     * A fallback buffer has to be defined because Iris tries to release that buffer, so it can't be null. It should be fine
     * to reuse/release the buffer multiple times, as it won't ever be written into by minecraft or Iris.
     */
    private final static BufferBuilder FALLBACK_BUFFER = new BufferBuilder(0);

    protected final Reference2ObjectMap<RenderLayer, ReferenceSet<BufferBuilder>> fallbackBuffers = new Reference2ObjectLinkedOpenHashMap<>();
    protected final ReferenceSet<RenderLayer> activeLayers = new ReferenceLinkedOpenHashSet<>();

    public ImmediateAdapter() {
        this(ImmutableMap.of());
    }

    public ImmediateAdapter(final Map<RenderLayer, BufferBuilder> layerBuffers) {
        super(FALLBACK_BUFFER, layerBuffers);
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
        this.fallbackBuffers.remove(layer);
    }

    @Override
    public void close() {
        this.activeLayers.clear();
        for (ReferenceSet<BufferBuilder> bufferBuilders : this.fallbackBuffers.values()) {
            for (BufferBuilder bufferBuilder : bufferBuilders) {
                if (bufferBuilder.isBuilding()) {
                    bufferBuilder.end().release();
                }
            }
        }
        this.fallbackBuffers.clear();
    }

    protected abstract void _draw(RenderLayer layer);

    protected BufferBuilder getOrCreateBufferBuilder(final RenderLayer layer) {
        if (!layer.areVerticesNotShared()) {
            return this.addNewFallbackBuffer(layer);
        } else if (this.layerBuffers.containsKey(layer)) {
            return this.layerBuffers.get(layer);
        } else if (this.fallbackBuffers.containsKey(layer)) {
            return this.fallbackBuffers.get(layer).iterator().next();
        } else {
            return this.addNewFallbackBuffer(layer);
        }
    }

    protected Set<BufferBuilder> getBufferBuilder(final RenderLayer layer) {
        if (this.fallbackBuffers.containsKey(layer)) {
            return this.fallbackBuffers.get(layer);
        } else if (this.layerBuffers.containsKey(layer)) {
            return Collections.singleton(this.layerBuffers.get(layer));
        } else {
            return Collections.emptySet();
        }
    }

    protected BufferBuilder addNewFallbackBuffer(final RenderLayer layer) {
        final BufferBuilder bufferBuilder = BufferBuilderPool.get();
        this.fallbackBuffers.computeIfAbsent(layer, k -> new ReferenceLinkedOpenHashSet<>()).add(bufferBuilder);
        return bufferBuilder;
    }

}
