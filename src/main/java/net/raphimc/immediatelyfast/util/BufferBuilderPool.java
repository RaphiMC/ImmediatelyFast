package net.raphimc.immediatelyfast.util;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.client.render.BufferBuilder;
import net.raphimc.immediatelyfast.injection.interfaces.IBufferBuilder;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

public class BufferBuilderPool {

    public static final int MAX_SIZE = 4096;
    private static final Set<Pair<BufferBuilder, Long>> POOL = new ReferenceArraySet<>(MAX_SIZE);

    private static long lastCleanup = 0;

    private BufferBuilderPool() {
    }

    public static BufferBuilder get() {
        if (lastCleanup < System.currentTimeMillis() - 5_000) {
            lastCleanup = System.currentTimeMillis();
            cleanup();
        }

        for (Pair<BufferBuilder, Long> entry : POOL) {
            final BufferBuilder bufferBuilder = entry.getKey();
            if (!bufferBuilder.isBuilding() && !((IBufferBuilder) bufferBuilder).isReleased()) {
                entry.setValue(System.currentTimeMillis());
                return bufferBuilder;
            }
        }

        if (POOL.size() >= MAX_SIZE) {
            throw new RuntimeException("BufferBuilder pool is exhausted");
        }

        final BufferBuilder bufferBuilder = new BufferBuilder(256);
        POOL.add(new MutablePair<>(bufferBuilder, System.currentTimeMillis()));
        return bufferBuilder;
    }

    public static int getAllocatedSize() {
        cleanup();
        return POOL.size();
    }

    private static void cleanup() {
        POOL.removeIf(b -> ((IBufferBuilder) b.getKey()).isReleased());
        POOL.removeIf(b -> {
            if (b.getValue() < System.currentTimeMillis() - 120_000) {
                ((IBufferBuilder) b.getKey()).release();
                return true;
            }
            return false;
        });
    }

}
