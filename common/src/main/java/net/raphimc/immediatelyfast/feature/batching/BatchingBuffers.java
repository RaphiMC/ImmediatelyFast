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
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.raphimc.immediatelyfast.ImmediatelyFast;

import java.util.Map;

/**
 * Class which holds various allocated buffers used for batching various rendered elements.
 * <p>
 * Also contains references to vertex consumers which are called within mixins to redirect the vertex data into the batching buffer.
 * <p>
 * Once a begin method is called, all vertex data between the begin and end method will be redirected into the batching buffer and drawn in one batch at the end.
 */
public class BatchingBuffers {

    /*
     * The references into which specific vertex data is redirected.
     *
     * Set to null if batching is disabled and the data should be drawn immediately as usual.
     */
    public static VertexConsumerProvider FILL_CONSUMER = null;
    public static VertexConsumerProvider TEXTURE_CONSUMER = null;
    public static VertexConsumerProvider TEXT_CONSUMER = null;
    public static VertexConsumerProvider ITEM_MODEL_CONSUMER = null;
    public static VertexConsumerProvider ITEM_OVERLAY_CONSUMER = null;

    /*
     * The batching buffers which hold the vertex data of the batch.
     */
    private static final BatchingBuffer HUD_BATCH = new BatchingBuffer();
    private static final BatchingBuffer ITEM_MODEL_BATCH = new ItemModelBatchableImmediate();
    private static final BatchingBuffer ITEM_OVERLAY_BATCH = new BatchingBuffer();

    public static void beginHudBatching() {
        if (HUD_BATCH.hasActiveLayers()) {
            ImmediatelyFast.LOGGER.warn("HUD batching was already active! endHudBatching() was not called before beginHudBatching(). This will cause rendering issues.");
            HUD_BATCH.close();
        }
        FILL_CONSUMER = HUD_BATCH;
        TEXTURE_CONSUMER = HUD_BATCH;
        TEXT_CONSUMER = HUD_BATCH;
        beginItemModelBatching();
        beginItemOverlayBatching();
    }

    public static void endHudBatching() {
        FILL_CONSUMER = null;
        TEXTURE_CONSUMER = null;
        TEXT_CONSUMER = null;
        final RenderSystemState renderSystemState = RenderSystemState.current();
        HUD_BATCH.draw();
        endItemModelBatching();
        endItemOverlayBatching();
        renderSystemState.apply();
    }

    public static boolean isHudBatching() {
        return TEXT_CONSUMER != null || TEXTURE_CONSUMER != null || FILL_CONSUMER != null || ITEM_MODEL_CONSUMER != null || ITEM_OVERLAY_CONSUMER != null;
    }

    public static boolean hasDataToDraw() {
        return HUD_BATCH.hasActiveLayers() || ITEM_MODEL_BATCH.hasActiveLayers() || ITEM_OVERLAY_BATCH.hasActiveLayers();
    }

    public static void forceDrawBuffers() {
        final RenderSystemState renderSystemState = RenderSystemState.current();
        HUD_BATCH.draw();
        ITEM_MODEL_BATCH.draw();
        ITEM_OVERLAY_BATCH.draw();
        renderSystemState.apply();
    }

    private static void beginItemModelBatching() {
        if (ITEM_MODEL_BATCH.hasActiveLayers()) {
            ImmediatelyFast.LOGGER.warn("Item model batching was already active! endItemModelBatching() was not called before beginItemModelBatching(). This will cause rendering issues.");
            ITEM_MODEL_BATCH.close();
        }
        ITEM_MODEL_CONSUMER = ITEM_MODEL_BATCH;
    }

    private static void endItemModelBatching() {
        ITEM_MODEL_CONSUMER = null;

        ITEM_MODEL_BATCH.draw();
    }

    private static void beginItemOverlayBatching() {
        if (ITEM_OVERLAY_BATCH.hasActiveLayers()) {
            ImmediatelyFast.LOGGER.warn("Item overlay batching was already active! endItemOverlayBatching() was not called before beginItemOverlayBatching(). This will cause rendering issues.");
            ITEM_OVERLAY_BATCH.close();
        }
        ITEM_OVERLAY_CONSUMER = ITEM_OVERLAY_BATCH;
    }

    private static void endItemOverlayBatching() {
        ITEM_OVERLAY_CONSUMER = null;
        ITEM_OVERLAY_BATCH.draw();
    }

    private static VertexConsumerProvider PREV_FILL_CONSUMER = null;
    private static VertexConsumerProvider PREV_TEXT_CONSUMER = null;
    private static VertexConsumerProvider PREV_TEXTURE_CONSUMER = null;

    public static void beginItemOverlayRendering() {
        if (ITEM_OVERLAY_CONSUMER != null) {
            PREV_FILL_CONSUMER = FILL_CONSUMER;
            PREV_TEXT_CONSUMER = TEXT_CONSUMER;
            PREV_TEXTURE_CONSUMER = TEXTURE_CONSUMER;
            FILL_CONSUMER = ITEM_OVERLAY_CONSUMER;
            TEXT_CONSUMER = ITEM_OVERLAY_CONSUMER;
            TEXTURE_CONSUMER = ITEM_OVERLAY_CONSUMER;
        }
    }

    public static void endItemOverlayRendering() {
        if (ITEM_OVERLAY_CONSUMER != null) {
            FILL_CONSUMER = PREV_FILL_CONSUMER;
            TEXT_CONSUMER = PREV_TEXT_CONSUMER;
            TEXTURE_CONSUMER = PREV_TEXTURE_CONSUMER;
        }
    }

    /**
     * Creates a map of layer buffers for the given RenderLayer's.
     *
     * @param layers The RenderLayer's for which to create the layer buffers.
     * @return A map of layer buffers for the given RenderLayer's.
     */
    public static Map<RenderLayer, BufferBuilder> createLayerBuffers(final RenderLayer... layers) {
        final Object2ObjectMap<RenderLayer, BufferBuilder> layerBuffers = new Object2ObjectLinkedOpenHashMap<>(layers.length);
        for (final RenderLayer layer : layers) {
            layerBuffers.put(layer, new BufferBuilder(layer.getExpectedBufferSize()));
        }
        return layerBuffers;
    }

}
