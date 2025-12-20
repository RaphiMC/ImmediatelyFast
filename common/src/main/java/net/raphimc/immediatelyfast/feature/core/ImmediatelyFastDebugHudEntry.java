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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture;
import net.raphimc.immediatelyfast.injection.interfaces.IMapTextureManager;
import net.raphimc.immediatelyfast.util.MathUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImmediatelyFastDebugHudEntry implements DebugHudEntry {

    public static final Identifier ENTRY_ID = Identifier.of("immediatelyfast", "immediatelyfast");
    private static final Identifier SECTION_ID = ENTRY_ID;

    @Override
    public void render(final DebugHudLines debugHudLines, final World world, final WorldChunk clientChunk, final WorldChunk chunk) {
        final List<String> lines = new ArrayList<>();
        lines.add("ImmediatelyFast " + ImmediatelyFast.VERSION);
        lines.add("Buffer Pool: " + BufferAllocatorPool.getSize() + " buffers (" + MathUtil.formatBytes(BufferAllocatorPool.getAllocatedBytes()) + ")");
        if (MinecraftClient.getInstance().getMapTextureManager() instanceof IMapTextureManager mapTextureManager) {
            final Collection<MapAtlasTexture> atlasTextures = mapTextureManager.immediatelyFast$getAllMapAtlasTextures();
            final int totalMapCount = atlasTextures.stream().mapToInt(MapAtlasTexture::getMapCount).sum();
            lines.add("Map Atlas: " + atlasTextures.size() + "x" + MapAtlasTexture.ATLAS_SIZE + "x" + MapAtlasTexture.ATLAS_SIZE + " (" + totalMapCount + " maps)");
        }
        if (ImmediatelyFast.signTextCache != null) {
            lines.add("Sign Text Cache: " + ImmediatelyFast.signTextCache.slotCache.size() + " entries");
        }
        debugHudLines.addLinesToSection(SECTION_ID, lines);
    }

    @Override
    public boolean canShow(final boolean reducedDebugInfo) {
        return true;
    }

}
