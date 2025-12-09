/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2025 RK_01/RaphiMC and contributors
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

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.util.IrisCompat;

import java.util.*;

public class BatchableBufferSource extends MultiBufferSource.BufferSource implements AutoCloseable {

    /**
     * A fallback buffer has to be defined because Iris tries to release that buffer, so it can't be null. It should be fine
     * to reuse/release the buffer multiple times, as it won't ever be written into by minecraft or Iris.
     */
    private final static ByteBufferBuilder FALLBACK_BUFFER = new ByteBufferBuilder(0);

    protected final Map<RenderType, ReferenceSet<BufferBuilder>> dynamicBuffers = IrisCompat.IRIS_LOADED ? new Object2ObjectLinkedOpenHashMap<>() : new Reference2ObjectLinkedOpenHashMap<>();
    protected final Set<RenderType> activeRenderTypes = IrisCompat.IRIS_LOADED ? new ObjectLinkedOpenHashSet<>() : new ReferenceLinkedOpenHashSet<>();

    protected boolean drawDynamicBuffersFirst = false;

    public BatchableBufferSource() {
        this(Object2ObjectSortedMaps.emptyMap());
    }

    public BatchableBufferSource(final SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers) {
        this(FALLBACK_BUFFER, fixedBuffers);
    }

    public BatchableBufferSource(final ByteBufferBuilder sharedBuffer, final SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers) {
        super(sharedBuffer, fixedBuffers);
    }

    @Override
    public VertexConsumer getBuffer(final RenderType renderType) {
        if (!this.drawDynamicBuffersFirst) {
            if (this.lastSharedType != null && this.lastSharedType != renderType && !this.fixedBuffers.containsKey(this.lastSharedType)) {
                this.drawDynamicBuffersFirst = true;
            }
        }

        if (IrisCompat.IRIS_LOADED) {
            IrisCompat.skipExtension.set(!IrisCompat.isRenderingLevel.getAsBoolean());
        }

        final BufferBuilder bufferBuilder;
        boolean hasBufferForRenderType = renderType.canConsolidateConsecutiveGeometry() && this.dynamicBuffers.containsKey(renderType);
        if (!renderType.canConsolidateConsecutiveGeometry()) {
            bufferBuilder = new BufferBuilder(this.getNextByteBufferBuilder(), renderType.mode(), renderType.format());
            this.lastSharedType = renderType;
        } else if (hasBufferForRenderType) {
            bufferBuilder = this.dynamicBuffers.get(renderType).iterator().next();
        } else if (this.fixedBuffers.containsKey(renderType)) {
            bufferBuilder = new BufferBuilder(this.fixedBuffers.get(renderType), renderType.mode(), renderType.format());
        } else {
            bufferBuilder = new BufferBuilder(this.getNextByteBufferBuilder(), renderType.mode(), renderType.format());
            this.lastSharedType = renderType;
        }

        if (IrisCompat.IRIS_LOADED) {
            IrisCompat.skipExtension.set(false);
        }

        if (!hasBufferForRenderType) {
            this.dynamicBuffers.computeIfAbsent(renderType, k -> new ReferenceLinkedOpenHashSet<>()).add(bufferBuilder);
        }

        if (hasBufferForRenderType) {
            if ((ImmediatelyFast.config.debug_only_use_last_usage_for_batch_ordering || renderType.name.contains("immediatelyfast:renderlast")) && this.activeRenderTypes.contains(renderType)) { // Fix for https://github.com/RaphiMC/ImmediatelyFast/issues/181
                this.activeRenderTypes.remove(renderType);
                this.activeRenderTypes.add(renderType);
            }
        } else {
            this.activeRenderTypes.add(renderType);
        }

        return bufferBuilder;
    }

    @Override
    public void endLastBatch() {
        this.lastSharedType = null;
        this.drawDynamicBuffersFirst = false;

        int sortedRenderTypesLength = 0;
        final RenderType[] sortedRenderTypes = new RenderType[this.activeRenderTypes.size()];
        for (RenderType renderType : this.activeRenderTypes) {
            if (!this.fixedBuffers.containsKey(renderType)) {
                sortedRenderTypes[sortedRenderTypesLength++] = renderType;
            }
        }
        if (sortedRenderTypesLength == 0) {
            return;
        }

        Arrays.sort(sortedRenderTypes, (t1, t2) -> Integer.compare(this.getRenderTypeOrder(t1), this.getRenderTypeOrder(t2)));
        for (int i = 0; i < sortedRenderTypesLength; i++) {
            this.endBatch(sortedRenderTypes[i]);
        }
    }

    @Override
    public void endBatch() {
        if (this.activeRenderTypes.isEmpty()) {
            this.close();
            return;
        }

        this.endLastBatch();
        for (RenderType renderType : this.fixedBuffers.keySet()) {
            this.endBatch(renderType);
        }
    }

    @Override
    public void endBatch(final RenderType renderType) {
        if (this.drawDynamicBuffersFirst) {
            this.endLastBatch();
        }

        this.drawDirect(renderType);
    }

    @Override
    public void close() {
        this.lastSharedType = null;
        this.drawDynamicBuffersFirst = false;

        for (RenderType renderType : this.activeRenderTypes) {
            for (BufferBuilder bufferBuilder : this.getBufferBuilder(renderType)) {
                bufferBuilder.build();
                ByteBufferBuilderPool.returnBufferBuilderSafe(bufferBuilder.buffer);
            }
        }

        this.activeRenderTypes.clear();
        this.dynamicBuffers.clear();
    }

    public void drawDirect(final RenderType renderType) {
        if (IrisCompat.IRIS_LOADED && !IrisCompat.isRenderingLevel.getAsBoolean()) {
            IrisCompat.renderWithExtendedVertexFormat.accept(false);
        }

        this.activeRenderTypes.remove(renderType);
        for (BufferBuilder bufferBuilder : this.getBufferBuilder(renderType)) {
            final ByteBufferBuilder prevBufferBuilder = this.sharedBuffer;
            this.sharedBuffer = bufferBuilder.buffer;
            this.endBatch(renderType, bufferBuilder);
            this.sharedBuffer = prevBufferBuilder;
            ByteBufferBuilderPool.returnBufferBuilderSafe(bufferBuilder.buffer);
        }
        this.dynamicBuffers.remove(renderType);
        if (this.lastSharedType == renderType) {
            this.lastSharedType = null;
        }

        if (IrisCompat.IRIS_LOADED && !IrisCompat.isRenderingLevel.getAsBoolean()) {
            IrisCompat.renderWithExtendedVertexFormat.accept(true);
        }
    }

    public boolean hasActiveRenderTypes() {
        return !this.activeRenderTypes.isEmpty();
    }

    protected Set<BufferBuilder> getBufferBuilder(final RenderType renderType) {
        if (this.dynamicBuffers.containsKey(renderType)) {
            return this.dynamicBuffers.get(renderType);
        } else {
            return Collections.emptySet();
        }
    }

    protected int getRenderTypeOrder(final RenderType renderType) {
        if (renderType == null) return Integer.MAX_VALUE;

        int order = 0;
        if (renderType instanceof RenderType.CompositeRenderType compositeRenderType) {
            final ResourceLocation textureId = compositeRenderType.state.textureState.cutoutTexture().orElse(null);
            if (textureId != null) {
                if (textureId.toString().startsWith("minecraft:textures/entity/wolf/")) {
                    if (textureId.equals(WolfCollarLayer.WOLF_COLLAR_LOCATION)) {
                        order = 2;
                    } else {
                        order = 1;
                    }
                } else if (textureId.equals(Sheets.ARMOR_TRIMS_SHEET)) {
                    order = 1;
                } else if (renderType.name.startsWith("text") || renderType.name.startsWith("neoforge_text")) {
                    // Draws vanilla text over custom font text
                    // Fixes https://github.com/RaphiMC/ImmediatelyFast/issues/81, https://github.com/RaphiMC/ImmediatelyFast/issues/287, https://github.com/RaphiMC/ImmediatelyFast/issues/288
                    if (textureId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                        order = 2;
                    } else {
                        order = 1;
                    }
                }
            }
        }

        if (!renderType.sortOnUpload()) {
            return order;
        } else {
            return 100_000_000 + order;
        }
    }

    private ByteBufferBuilder getNextByteBufferBuilder() {
        if (this.sharedBuffer != FALLBACK_BUFFER && this.lastSharedType == null && this.sharedBuffer.pointer != 0L) {
            return this.sharedBuffer;
        } else {
            return ByteBufferBuilderPool.borrowBufferBuilder();
        }
    }

}
