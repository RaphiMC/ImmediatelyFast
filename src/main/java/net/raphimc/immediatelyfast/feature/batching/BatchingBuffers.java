package net.raphimc.immediatelyfast.feature.batching;

import net.minecraft.client.render.VertexConsumerProvider;
import net.raphimc.immediatelyfast.feature.core.BatchableImmediate;

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
    public static VertexConsumerProvider TEXTURE_CONSUMER = null;
    public static VertexConsumerProvider FILL_CONSUMER = null;
    public static VertexConsumerProvider TEXT_CONSUMER = null;

    /*
     * The batching buffers which hold the vertex data of the batch.
     */
    private static final BatchableImmediate TEXTURE_BATCH = new BatchableImmediate();
    private static final BatchableImmediate FILL_BATCH = new BatchableImmediate();
    private static final BatchableImmediate TEXT_BATCH = new BatchableImmediate();

    public static void beginHudBatching() {
        beginFillBatching();
        beginTextureBatching();
        beginTextBatching();
    }

    public static void endHudBatching() {
        endFillBatching();
        endTextureBatching();
        endTextBatching();
    }

    public static void beginTextureBatching() {
        TEXTURE_BATCH.close();
        TEXTURE_CONSUMER = TEXTURE_BATCH;
    }

    public static void endTextureBatching() {
        TEXTURE_CONSUMER = null;
        TEXTURE_BATCH.draw();
    }

    public static void beginFillBatching() {
        FILL_BATCH.close();
        FILL_CONSUMER = FILL_BATCH;
    }

    public static void endFillBatching() {
        FILL_CONSUMER = null;
        FILL_BATCH.draw();
    }

    public static void beginTextBatching() {
        TEXT_BATCH.close();
        TEXT_CONSUMER = TEXT_BATCH;
    }

    public static void endTextBatching() {
        TEXT_CONSUMER = null;
        TEXT_BATCH.draw();
    }

}
