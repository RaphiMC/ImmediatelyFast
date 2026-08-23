/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.immediatelyfast.feature.batch_animated_item_updates;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.render.DynamicAtlasAllocator;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.util.Mth;

/**
 * Implementation of GuiItemAtlas which clears the full texture after each frame, so that future uses don't need to clear each slot individually
 */
public class AnimatedItemAtlas extends GuiItemAtlas {

    private static final int MINIMUM_TEXTURE_SIZE = 128;

    private int lastUsedSlotCount;

    public static int computeTextureSizeFor(final int slotTextureSize, final int requiredSlotCount) {
        final int preferredSlotCount = requiredSlotCount + requiredSlotCount / 2;
        final int atlasSize = Mth.smallestSquareSide(preferredSlotCount);
        return Math.clamp(Mth.smallestEncompassingPowerOfTwo(atlasSize * slotTextureSize), MINIMUM_TEXTURE_SIZE, RenderSystem.getDevice().getDeviceInfo().limits().maxTextureSizeForFormat(GpuFormat.RGBA8_UNORM));
    }

    public AnimatedItemAtlas(final FeatureRenderDispatcher featureRenderDispatcher, final int textureSize, final int slotTextureSize) {
        super(featureRenderDispatcher, textureSize, slotTextureSize);
    }

    @Override
    public SlotView getOrUpdate(final TrackingItemStackRenderState item) {
        if (!item.isAnimated()) {
            throw new IllegalArgumentException("Item is not animated");
        }
        return super.getOrUpdate(item);
    }

    @Override
    public void endFrame() {
        this.lastUsedSlotCount = this.allocator.usedSlotByKey.size();
        if (this.lastUsedSlotCount > 0) {
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(this.texture, GuiRenderer.CLEAR_COLOR, this.depthTexture, 0D);
            for (DynamicAtlasAllocator.Slot slot : this.allocator.usedSlotByKey.values()) {
                slot.fresh = true;
            }
        }
        super.endFrame();
    }

    public int getLastUsedSlotCount() {
        return this.lastUsedSlotCount;
    }

}
