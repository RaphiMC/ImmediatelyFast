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
package net.raphimc.immediatelyfast.feature.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.batch_animated_item_updates.AnimatedItemAtlas;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture;
import net.raphimc.immediatelyfast.injection.interfaces.IGuiRenderer;
import net.raphimc.immediatelyfast.injection.interfaces.IMapTextureManager;
import net.raphimc.immediatelyfast.util.MathUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImmediatelyFastDebugScreenEntry implements DebugScreenEntry {

    public static final Identifier ENTRY_ID = Identifier.fromNamespaceAndPath("immediatelyfast", "immediatelyfast");
    private static final Identifier SECTION_ID = ENTRY_ID;

    @Override
    public void display(final DebugScreenDisplayer displayer, final Level level, final LevelChunk clientChunk, final LevelChunk serverChunk) {
        final List<String> lines = new ArrayList<>();
        lines.add("ImmediatelyFast " + ImmediatelyFast.VERSION);
        lines.add("Buffer Pool: " + ByteBufferBuilderPool.getSize() + " buffers (" + MathUtil.formatBytes(ByteBufferBuilderPool.getAllocatedBytes()) + ")");
        if (Minecraft.getInstance().getMapTextureManager() instanceof IMapTextureManager mapTextureManager) {
            final Collection<MapAtlasTexture> atlasTextures = mapTextureManager.immediatelyFast$getAllMapAtlasTextures();
            final int totalMapCount = atlasTextures.stream().mapToInt(MapAtlasTexture::getMapCount).sum();
            lines.add("Map Atlas: " + atlasTextures.size() + "x" + MapAtlasTexture.ATLAS_SIZE + "x" + MapAtlasTexture.ATLAS_SIZE + " (" + totalMapCount + " maps)");
        }
        if (Minecraft.getInstance().gameRenderer.guiRenderer instanceof IGuiRenderer guiRenderer) {
            final AnimatedItemAtlas animatedItemAtlas = guiRenderer.immediatelyFast$getAnimatedItemAtlas();
            if (animatedItemAtlas != null) {
                lines.add("Animated Item Atlas: " + animatedItemAtlas.textureSize() + "x" + animatedItemAtlas.textureSize() + " (" + animatedItemAtlas.getLastUsedSlotCount() + " items)");
            } else {
                lines.add("Animated Item Atlas: Not allocated");
            }
        }
        if (ImmediatelyFast.signTextCache != null) {
            lines.add("Sign Text Cache: " + ImmediatelyFast.signTextCache.slotCache.size() + " entries");
        }
        displayer.addToGroup(SECTION_ID, lines);
    }

    @Override
    public boolean isAllowed(final boolean reducedDebugInfo) {
        return true;
    }

}
