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
package net.raphimc.immediatelyfast.feature.map_atlas_generation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.util.GpuOnlyTexture;

public class MapAtlas implements AutoCloseable {

    public static final int ATLAS_SIZE = ImmediatelyFast.config.map_atlas_size;
    public static final int MAP_SIZE = 128;
    public static final int MAPS_PER_ATLAS = (ATLAS_SIZE / MAP_SIZE) * (ATLAS_SIZE / MAP_SIZE);

    public static int getAtlasIdFromLocation(final int location) {
        return (location >> 16) & 0xFFFF;
    }

    public static int getAtlasXFromLocation(final int location) {
        return (location >> 8) & 0xFF;
    }

    public static int getAtlasYFromLocation(final int location) {
        return location & 0xFF;
    }

    private final int id;
    private final Identifier textureId;
    private final AbstractTexture texture;
    private int mapCount;

    public MapAtlas(final int id) {
        this.id = id;
        this.textureId = Identifier.fromNamespaceAndPath("immediatelyfast", "map_atlas/" + id);
        this.texture = new GpuOnlyTexture("ImmediatelyFast Map Atlas " + id, ATLAS_SIZE, ATLAS_SIZE);
        Minecraft.getInstance().getTextureManager().register(this.textureId, this.texture);
    }

    public int getNextLocation() {
        if (this.mapCount >= MAPS_PER_ATLAS) {
            return -1;
        }

        final int atlasX = this.mapCount % (ATLAS_SIZE / MAP_SIZE);
        final int atlasY = this.mapCount / (ATLAS_SIZE / MAP_SIZE);
        if (this.id < 0 || this.id >= 65536 || atlasX < 0 || atlasX >= 256 || atlasY < 0 || atlasY >= 256) {
            throw new IllegalStateException("Invalid map atlas location: id=" + this.id + ", atlasX=" + atlasX + ", atlasY=" + atlasY);
        }
        this.mapCount++;

        return (this.id << 16) | (atlasX << 8) | atlasY;
    }

    public int getId() {
        return this.id;
    }

    public Identifier getTextureId() {
        return this.textureId;
    }

    public AbstractTexture getTexture() {
        return this.texture;
    }

    public int getMapCount() {
        return this.mapCount;
    }

    @Override
    public void close() {
        Minecraft.getInstance().getTextureManager().release(this.textureId);
    }

}
