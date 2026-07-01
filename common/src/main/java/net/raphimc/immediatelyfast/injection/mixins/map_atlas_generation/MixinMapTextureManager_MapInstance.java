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

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlas;
import net.raphimc.immediatelyfast.injection.interfaces.IMapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.resources.MapTextureManager$MapInstance")
public abstract class MixinMapTextureManager_MapInstance {

    @Shadow
    @Final
    private DynamicTexture texture;

    @Unique
    private AbstractTexture immediatelyFast$atlasTexture;

    @Unique
    private int immediatelyFast$atlasX;

    @Unique
    private int immediatelyFast$atlasY;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void getAtlasParameters(final MapTextureManager mapTextureManager, final int id, final MapItemSavedData data, final CallbackInfo ci) {
        if (!ImmediatelyFast.runtimeConfig.map_atlas_generation) {
            return;
        }
        final int location = ((IMapTextureManager) mapTextureManager).immediatelyFast$getAtlasLocation(id);
        if (location != -1) {
            this.immediatelyFast$atlasTexture = ((IMapTextureManager) mapTextureManager).immediatelyFast$getAtlas(MapAtlas.getAtlasIdFromLocation(location)).getTexture();
            this.immediatelyFast$atlasX = MapAtlas.getAtlasXFromLocation(location) * MapAtlas.MAP_SIZE;
            this.immediatelyFast$atlasY = MapAtlas.getAtlasYFromLocation(location) * MapAtlas.MAP_SIZE;
        } else {
            ImmediatelyFast.LOGGER.warn("Map " + id + " has no atlas location, falling back to vanilla behavior.");
        }
    }

    @Inject(method = "updateTextureIfNeeded", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V", shift = At.Shift.AFTER))
    private void updateAtlasTexture(final CallbackInfo ci) {
        if (this.immediatelyFast$atlasTexture != null && ImmediatelyFast.runtimeConfig.map_atlas_generation) {
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.immediatelyFast$atlasTexture.getTexture(), this.texture.getPixels(), 0, 0, this.immediatelyFast$atlasX, this.immediatelyFast$atlasY);
        }
    }

}
