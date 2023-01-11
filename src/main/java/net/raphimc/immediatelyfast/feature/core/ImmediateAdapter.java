/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.immediatelyfast.feature.core;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.raphimc.immediatelyfast.util.ImmediateUtil.sharedVerticesComparator;

public abstract class ImmediateAdapter extends VertexConsumerProvider.Immediate implements AutoCloseable {

    /**
     * A fallback buffer has to be defined because Iris tries to release that buffer, so it can't be null. It should be fine
     * to reuse/release the buffer multiple times, as it won't ever be written into by minecraft or Iris.
     */
    private final static BufferBuilder FALLBACK_BUFFER = new BufferBuilder(0);

    protected final Reference2ObjectMap<RenderLayer, ReferenceSet<BufferBuilder>> fallbackBuffers = new Reference2ObjectLinkedOpenHashMap<>();
    protected final ReferenceSet<RenderLayer> activeLayers = new ReferenceLinkedOpenHashSet<>();

    private boolean drawFallbackLayersFirst = false;

    public ImmediateAdapter() {
        this(ImmutableMap.of());
    }

    public ImmediateAdapter(final Map<RenderLayer, BufferBuilder> layerBuffers) {
        super(FALLBACK_BUFFER, layerBuffers);
    }

    @Override
    public VertexConsumer getBuffer(final RenderLayer layer) {
        final BufferBuilder bufferBuilder = this.getOrCreateBufferBuilder(layer);
        if (bufferBuilder.isBuilding() && sharedVerticesComparator(layer.getDrawMode().size)) {
            throw new IllegalStateException("Tried to write shared vertices into the same buffer");
        }

        if (!this.drawFallbackLayersFirst) {
            final Optional<RenderLayer> newLayer = layer.asOptional();
            if (!this.currentLayer.equals(newLayer)) {
                if (this.currentLayer.isPresent() && !this.layerBuffers.containsKey(this.currentLayer.get())) {
                    this.drawFallbackLayersFirst = true;
                }
            }
            this.currentLayer = newLayer;
        }

        if (!bufferBuilder.isBuilding()) {
            bufferBuilder.begin(layer.getDrawMode(), layer.getVertexFormat());
            this.activeLayers.add(layer);
        }
        return bufferBuilder;
    }

    @Override
    public void drawCurrentLayer() {
        this.currentLayer = Optional.empty();
        this.drawFallbackLayersFirst = false;

        this.activeLayers.stream().filter(l -> !this.layerBuffers.containsKey(l)).sorted((l1, l2) -> {
            if (l1.translucent == l2.translucent) return 0;
            return l1.translucent ? 1 : -1;
        }).forEachOrdered(this::draw);
    }

    @Override
    public void draw() {
        if (this.activeLayers.isEmpty()) {
            this.close();
            return;
        }

        this.drawCurrentLayer();
        for (RenderLayer layer : this.layerBuffers.keySet()) {
            this.draw(layer);
        }
    }

    @Override
    public void draw(final RenderLayer layer) {
        if (this.drawFallbackLayersFirst) {
            this.drawCurrentLayer();
        }

        this.activeLayers.remove(layer);
        this._draw(layer);
        this.fallbackBuffers.remove(layer);
    }

    @Override
    public void close() {
        this.currentLayer = Optional.empty();
        this.drawFallbackLayersFirst = false;

        for (RenderLayer layer : this.activeLayers) {
            for (BufferBuilder bufferBuilder : this.getBufferBuilder(layer)) {
                bufferBuilder.end();//.release();
                bufferBuilder.clear();
            }
        }

        this.activeLayers.clear();
        this.fallbackBuffers.clear();
    }

    public boolean hasActiveLayers() {
        return !this.activeLayers.isEmpty();
    }

    protected abstract void _draw(RenderLayer layer);

    protected BufferBuilder getOrCreateBufferBuilder(final RenderLayer layer) {
        if (sharedVerticesComparator(layer.getDrawMode().size)) {
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
