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
package net.raphimc.immediatelyfast.feature.batching;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.BufferAllocator;
import net.raphimc.immediatelyfast.feature.core.BatchableBufferSource;

import java.util.SequencedMap;
import java.util.Set;

public class HudBatchingBufferSource extends BatchableBufferSource {

    private final Object2ObjectMap<ReferenceObjectPair<RenderLayer, LightingState>, RenderLayer> lightingRenderLayers = new Object2ObjectOpenHashMap<>();
    private final Reference2ObjectMap<RenderLayer, ReferenceSet<RenderLayer>> renderLayerMap = new Reference2ObjectOpenHashMap<>();
    private boolean renderingItem = false;

    public HudBatchingBufferSource(final BufferAllocator fallbackBuffer, final SequencedMap<RenderLayer, BufferAllocator> layerBuffers) {
        super(fallbackBuffer, layerBuffers);
    }

    public void setRenderingItem(final boolean renderingItem) {
        this.renderingItem = renderingItem;
    }

    @Override
    public VertexConsumer getBuffer(final RenderLayer layer) {
        if (!this.renderingItem || layer.name.contains("glint")) {
            return super.getBuffer(layer);
        }

        final LightingState lightingState = LightingState.current();
        final RenderLayer newLayer = this.lightingRenderLayers.computeIfAbsent(new ReferenceObjectImmutablePair<>(layer, lightingState), key -> new BatchingBuffers.WrappedRenderLayer(layer, lightingState::saveAndApply, lightingState::revert));
        this.renderLayerMap.computeIfAbsent(layer, key -> new ReferenceOpenHashSet<>()).add(newLayer);
        return super.getBuffer(newLayer);
    }

    @Override
    public void drawDirect(final RenderLayer layer) {
        final Set<RenderLayer> renderLayers = this.renderLayerMap.remove(layer);
        if (renderLayers != null) {
            for (RenderLayer renderLayer : renderLayers) {
                super.drawDirect(renderLayer);
            }
        } else {
            super.drawDirect(layer);
        }
    }

    @Override
    public void draw() {
        super.draw();
        this.lightingRenderLayers.clear();
        this.renderLayerMap.clear();
    }

    @Override
    public void close() {
        super.close();
        this.lightingRenderLayers.clear();
        this.renderLayerMap.clear();
    }

}
