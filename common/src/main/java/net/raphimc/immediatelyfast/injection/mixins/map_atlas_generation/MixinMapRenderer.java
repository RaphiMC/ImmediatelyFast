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
package net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.item.map.MapId;
import net.minecraft.item.map.MapState;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture;
import net.raphimc.immediatelyfast.injection.interfaces.IMapRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapRenderer.class)
public abstract class MixinMapRenderer implements IMapRenderer {

    @Unique
    private final Int2ObjectMap<MapAtlasTexture> immediatelyFast$mapAtlasTextures = new Int2ObjectOpenHashMap<>();

    @Unique
    private final Int2IntMap immediatelyFast$mapIdToAtlasMapping = new Int2IntOpenHashMap();

    @Inject(method = "clearStateTextures", at = @At("RETURN"))
    private void clearMapAtlasTextures(final CallbackInfo ci) {
        for (MapAtlasTexture texture : this.immediatelyFast$mapAtlasTextures.values()) {
            texture.close();
        }

        this.immediatelyFast$mapAtlasTextures.clear();
        this.immediatelyFast$mapIdToAtlasMapping.clear();
    }

    @Inject(method = "getMapTexture", at = @At("HEAD"))
    private void createMapAtlasTexture(MapId mapId, MapState state, CallbackInfoReturnable<MapRenderer.MapTexture> cir) {
        this.immediatelyFast$mapIdToAtlasMapping.computeIfAbsent(mapId.id(), k -> {
            for (MapAtlasTexture atlasTexture : this.immediatelyFast$mapAtlasTextures.values()) {
                final int location = atlasTexture.getNextMapLocation();
                if (location != -1) {
                    return location;
                }
            }

            final MapAtlasTexture atlasTexture = new MapAtlasTexture(this.immediatelyFast$mapAtlasTextures.size());
            this.immediatelyFast$mapAtlasTextures.put(atlasTexture.getId(), atlasTexture);
            return atlasTexture.getNextMapLocation();
        });
    }

    @Override
    public MapAtlasTexture immediatelyFast$getMapAtlasTexture(int id) {
        return this.immediatelyFast$mapAtlasTextures.get(id);
    }

    @Override
    public int immediatelyFast$getAtlasMapping(int mapId) {
        return this.immediatelyFast$mapIdToAtlasMapping.getOrDefault(mapId, -1);
    }

}
