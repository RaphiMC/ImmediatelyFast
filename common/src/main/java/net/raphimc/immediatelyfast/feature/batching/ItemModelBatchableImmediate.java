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

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceObjectImmutablePair;
import it.unimi.dsi.fastutil.objects.ReferenceObjectPair;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.screen.PlayerScreenHandler;

public class ItemModelBatchableImmediate extends BatchingBuffer {

    private final Object2ObjectMap<ReferenceObjectPair<RenderLayer, LightingState>, RenderLayer> lightingRenderLayers = new Object2ObjectOpenHashMap<>();

    public ItemModelBatchableImmediate() {
        super(BatchingBuffers.createLayerBuffers(
                RenderLayer.getArmorGlint(),
                RenderLayer.getArmorEntityGlint(),
                RenderLayer.getGlint(),
                RenderLayer.getDirectGlint(),
                RenderLayer.getGlintTranslucent(),
                RenderLayer.getEntityGlint(),
                RenderLayer.getDirectEntityGlint()
        ));
    }

    @Override
    public VertexConsumer getBuffer(final RenderLayer layer) {
        final LightingState lightingState = LightingState.current();
        return super.getBuffer(this.lightingRenderLayers.computeIfAbsent(new ReferenceObjectImmutablePair<>(layer, lightingState), key -> new BatchingRenderLayers.WrappedRenderLayer(layer, lightingState::saveAndApply, lightingState::revert)));
    }

    @Override
    public void draw() {
        RenderSystem.getModelViewStack().push();
        RenderSystem.getModelViewStack().loadIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableBlend();
        super.draw();
        RenderSystem.disableBlend();
        this.lightingRenderLayers.clear();
        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();

    }

}
