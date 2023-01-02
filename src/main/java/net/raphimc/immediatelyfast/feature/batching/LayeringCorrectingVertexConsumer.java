package net.raphimc.immediatelyfast.feature.batching;

import net.minecraft.client.render.VertexConsumer;
import net.raphimc.immediatelyfast.util.ImmediateUtil;

public class LayeringCorrectingVertexConsumer implements VertexConsumer {

    private static float Z_OFFSET = 0F; // Fix Z layering issues with overlapping 2D elements

    private final VertexConsumer delegate;

    public LayeringCorrectingVertexConsumer(final VertexConsumer delegate) {
        this.delegate = delegate;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return this.delegate.vertex(x, y, z + Z_OFFSET);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this.delegate.color(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this.delegate.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this.delegate.overlay(u, v);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this.delegate.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this.delegate.normal(x, y, z);
    }

    @Override
    public void next() {
        this.delegate.next();
    }

    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {
        this.delegate.fixedColor(red, green, blue, alpha);
    }

    @Override
    public void unfixColor() {
        this.delegate.unfixColor();
    }


    /**
     * Increases the Z offset by a small amount.
     */
    public static void incrementZOffset() {
        Z_OFFSET += 0.0001F;
    }

    /**
     * Resets the Z offset to 0.
     * Called at the end of the frame.
     */
    public static void resetZOffset() {
        Z_OFFSET = 0F;
    }

}
