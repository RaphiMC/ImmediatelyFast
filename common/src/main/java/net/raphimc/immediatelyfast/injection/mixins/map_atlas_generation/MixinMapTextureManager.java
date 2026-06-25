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
package net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlas;
import net.raphimc.immediatelyfast.injection.interfaces.IMapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(MapTextureManager.class)
public abstract class MixinMapTextureManager implements IMapTextureManager {

    @Unique
    private final Int2ObjectMap<MapAtlas> immediatelyFast$atlases = new Int2ObjectOpenHashMap<>();

    @Unique
    private final Int2IntMap immediatelyFast$mapIdToAtlasLocation = new Int2IntOpenHashMap();

    @Inject(method = "resetData", at = @At("RETURN"))
    private void resetAtlases(final CallbackInfo ci) {
        for (MapAtlas atlas : this.immediatelyFast$atlases.values()) {
            atlas.close();
        }
        this.immediatelyFast$atlases.clear();
        this.immediatelyFast$mapIdToAtlasLocation.clear();
    }

    @Inject(method = "getOrCreateMapInstance", at = @At("HEAD"))
    private void ensureHasAtlasLocation(final MapId id, final MapItemSavedData data, final CallbackInfoReturnable<?> cir) {
        this.immediatelyFast$mapIdToAtlasLocation.computeIfAbsent(id.id(), _ -> {
            for (MapAtlas atlas : this.immediatelyFast$atlases.values()) {
                final int location = atlas.getNextLocation();
                if (location != -1) {
                    return location;
                }
            }

            final MapAtlas atlas = new MapAtlas(this.immediatelyFast$atlases.size());
            this.immediatelyFast$atlases.put(atlas.getId(), atlas);
            return atlas.getNextLocation();
        });
    }

    @Override
    public MapAtlas immediatelyFast$getAtlas(final int id) {
        return this.immediatelyFast$atlases.get(id);
    }

    @Override
    public int immediatelyFast$getAtlasLocation(final int mapId) {
        return this.immediatelyFast$mapIdToAtlasLocation.getOrDefault(mapId, -1);
    }

    @Override
    public Collection<MapAtlas> immediatelyFast$getAtlases() {
        return this.immediatelyFast$atlases.values();
    }

}
