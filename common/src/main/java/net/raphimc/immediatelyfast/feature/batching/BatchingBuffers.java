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

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;

import java.util.SequencedMap;
import java.util.Set;

public class BatchingBuffers {

    private static VertexConsumerProvider.Immediate nonBatchingEntityVertexConsumers;
    private static VertexConsumerProvider.Immediate hudBatchingVertexConsumers;

    public static VertexConsumerProvider.Immediate getNonBatchingEntityVertexConsumers() {
        if (nonBatchingEntityVertexConsumers == null) {
            final SequencedMap<RenderLayer, BufferAllocator> layerBuffers = createLayerBuffers(MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().layerBuffers.keySet());
            nonBatchingEntityVertexConsumers = new VertexConsumerProvider.Immediate(new BufferAllocator(786432), layerBuffers);
        }
        return nonBatchingEntityVertexConsumers;
    }

    public static VertexConsumerProvider.Immediate getHudBatchingVertexConsumers() {
        if (hudBatchingVertexConsumers == null) {
            final SequencedMap<RenderLayer, BufferAllocator> layerBuffers = createLayerBuffers(MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().layerBuffers.keySet());
            hudBatchingVertexConsumers = new HudBatchingBufferSource(new BufferAllocator(786432), layerBuffers);
        }
        return hudBatchingVertexConsumers;
    }

    private static SequencedMap<RenderLayer, BufferAllocator> createLayerBuffers(final Set<RenderLayer> layers) {
        final SequencedMap<RenderLayer, BufferAllocator> layerBuffers = new Object2ObjectLinkedOpenHashMap<>(layers.size());
        for (final RenderLayer layer : layers) {
            layerBuffers.put(layer, new BufferAllocator(layer.getExpectedBufferSize()));
        }
        return layerBuffers;
    }

    public static class WrappedRenderLayer extends RenderLayer {

        public WrappedRenderLayer(final RenderLayer renderLayer, final Runnable additionalStartAction, final Runnable additionalEndAction) {
            super(renderLayer.name, renderLayer.getVertexFormat(), renderLayer.getDrawMode(), renderLayer.getExpectedBufferSize(), renderLayer.hasCrumbling(), renderLayer.isTranslucent(), () -> {
                renderLayer.startDrawing();
                additionalStartAction.run();
            }, () -> {
                renderLayer.endDrawing();
                additionalEndAction.run();
            });
        }

    }

}
