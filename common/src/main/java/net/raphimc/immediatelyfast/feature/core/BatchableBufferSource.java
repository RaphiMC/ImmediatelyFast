/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.compat.IrisCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.SequencedMap;
import java.util.Set;

public class BatchableBufferSource extends VertexConsumerProvider.Immediate implements AutoCloseable {

    /**
     * A fallback buffer has to be defined because Iris tries to release that buffer, so it can't be null. It should be fine
     * to reuse/release the buffer multiple times, as it won't ever be written into by minecraft or Iris.
     */
    private final static BufferAllocator FALLBACK_BUFFER = new BufferAllocator(0);

    protected final Reference2ObjectMap<RenderLayer, ReferenceSet<BufferBuilder>> pendingBuffers = new Reference2ObjectLinkedOpenHashMap<>();
    protected final ReferenceSet<RenderLayer> activeLayers = new ReferenceLinkedOpenHashSet<>();

    protected boolean drawFallbackLayersFirst = false;

    public BatchableBufferSource() {
        this(Object2ObjectSortedMaps.emptyMap());
    }

    public BatchableBufferSource(final SequencedMap<RenderLayer, BufferAllocator> layerBuffers) {
        this(FALLBACK_BUFFER, layerBuffers);
    }

    public BatchableBufferSource(final BufferAllocator fallbackBuffer, final SequencedMap<RenderLayer, BufferAllocator> layerBuffers) {
        super(fallbackBuffer, layerBuffers);
    }

    @Override
    public VertexConsumer getBuffer(final RenderLayer layer) {
        if (!this.drawFallbackLayersFirst) {
            if (this.currentLayer != null && this.currentLayer != layer && !this.layerBuffers.containsKey(this.currentLayer)) {
                this.drawFallbackLayersFirst = true;
            }
        }

        if (IrisCompat.IRIS_LOADED) {
            IrisCompat.skipExtension.set(!IrisCompat.isRenderingLevel.getAsBoolean());
        }

        final BufferBuilder bufferBuilder;
        boolean hasBufferForRenderLayer = layer.areVerticesNotShared() && this.pendingBuffers.containsKey(layer);
        if (!layer.areVerticesNotShared()) {
            bufferBuilder = new BufferBuilder(BufferAllocatorPool.borrowBufferAllocator(), layer.getDrawMode(), layer.getVertexFormat());
            this.currentLayer = layer;
        } else if (hasBufferForRenderLayer) {
            bufferBuilder = this.pendingBuffers.get(layer).iterator().next();
        } else if (this.layerBuffers.containsKey(layer)) {
            bufferBuilder = new BufferBuilder(this.layerBuffers.get(layer), layer.getDrawMode(), layer.getVertexFormat());
        } else {
            bufferBuilder = new BufferBuilder(BufferAllocatorPool.borrowBufferAllocator(), layer.getDrawMode(), layer.getVertexFormat());
            this.currentLayer = layer;
        }

        if (IrisCompat.IRIS_LOADED) {
            IrisCompat.skipExtension.set(false);
        }

        if (!hasBufferForRenderLayer) {
            this.pendingBuffers.computeIfAbsent(layer, k -> new ReferenceLinkedOpenHashSet<>()).add(bufferBuilder);
        }

        if (hasBufferForRenderLayer) {
            if ((ImmediatelyFast.config.debug_only_use_last_usage_for_batch_ordering || layer.name.contains("immediatelyfast:renderlast")) && this.activeLayers.contains(layer)) { // Fix for https://github.com/RaphiMC/ImmediatelyFast/issues/181
                this.activeLayers.remove(layer);
                this.activeLayers.add(layer);
            }
        } else {
            this.activeLayers.add(layer);
        }

        return bufferBuilder;
    }

    @Override
    public void drawCurrentLayer() {
        this.currentLayer = null;
        this.drawFallbackLayersFirst = false;

        int sortedLayersLength = 0;
        final RenderLayer[] sortedLayers = new RenderLayer[this.activeLayers.size()];
        for (RenderLayer layer : this.activeLayers) {
            if (!this.layerBuffers.containsKey(layer)) {
                sortedLayers[sortedLayersLength++] = layer;
            }
        }
        if (sortedLayersLength == 0) {
            return;
        }

        Arrays.sort(sortedLayers, (l1, l2) -> Integer.compare(this.getLayerOrder(l1), this.getLayerOrder(l2)));
        for (int i = 0; i < sortedLayersLength; i++) {
            this.draw(sortedLayers[i]);
        }
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

        if (IrisCompat.IRIS_LOADED && !IrisCompat.isRenderingLevel.getAsBoolean()) {
            IrisCompat.renderWithExtendedVertexFormat.accept(false);
        }

        this.activeLayers.remove(layer);
        for (BufferBuilder bufferBuilder : this.getBufferBuilder(layer)) {
            final BufferAllocator prevBufferAllocator = bufferBuilder.allocator;
            this.allocator = bufferBuilder.allocator;
            this.draw(layer, bufferBuilder);
            this.allocator = prevBufferAllocator;
            BufferAllocatorPool.returnBufferAllocatorSafe(bufferBuilder.allocator);
        }
        this.pendingBuffers.remove(layer);

        if (IrisCompat.IRIS_LOADED && !IrisCompat.isRenderingLevel.getAsBoolean()) {
            IrisCompat.renderWithExtendedVertexFormat.accept(true);
        }
    }

    @Override
    public void close() {
        this.currentLayer = null;
        this.drawFallbackLayersFirst = false;

        for (RenderLayer layer : this.activeLayers) {
            for (BufferBuilder bufferBuilder : this.getBufferBuilder(layer)) {
                bufferBuilder.endNullable();
                BufferAllocatorPool.returnBufferAllocatorSafe(bufferBuilder.allocator);
            }
        }

        this.activeLayers.clear();
        this.pendingBuffers.clear();
    }

    public boolean hasActiveLayers() {
        return !this.activeLayers.isEmpty();
    }

    protected Set<BufferBuilder> getBufferBuilder(final RenderLayer layer) {
        if (this.pendingBuffers.containsKey(layer)) {
            return this.pendingBuffers.get(layer);
        } else {
            return Collections.emptySet();
        }
    }

    protected int getLayerOrder(final RenderLayer layer) {
        if (layer == null) return Integer.MAX_VALUE;
        if (layer instanceof RenderLayer.MultiPhase multiPhase) {
            final Identifier textureId = multiPhase.getPhases().texture.getId().orElse(null);
            if (textureId != null) {
                if (textureId.toString().startsWith("minecraft:textures/entity/horse/")) {
                    final String horseTexturePath = textureId.toString().substring(("minecraft:textures/entity/horse/").length());
                    if (horseTexturePath.startsWith("horse_markings")) {
                        return 2;
                    } else if (horseTexturePath.startsWith("armor/")) {
                        return 3;
                    } else {
                        return 1;
                    }
                } else if (textureId.equals(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE)) {
                    return 1;
                }
            }
        }

        if (!layer.isTranslucent()) {
            return Integer.MIN_VALUE;
        } else {
            return Integer.MAX_VALUE - 1;
        }
    }

}
