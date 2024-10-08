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

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.MapTextureManager;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.injection.interfaces.IMapRenderState;
import net.raphimc.immediatelyfast.injection.interfaces.IMapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture.ATLAS_SIZE;
import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture.MAP_SIZE;

@Mixin(value = MapRenderer.class, priority = 1100) // Workaround for Porting-Lib which relies on the LVT to be intact
public abstract class MixinMapRenderer {

    @Shadow
    @Final
    private MapTextureManager textureManager;

    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;texture(FF)Lnet/minecraft/client/render/VertexConsumer;"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lnet/minecraft/client/render/VertexConsumer;", ordinal = 0), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;light(I)Lnet/minecraft/client/render/VertexConsumer;", ordinal = 3)))
    private VertexConsumer drawAtlasTexture(VertexConsumer instance, float u, float v, @Local(argsOnly = true) MapRenderState renderState) {
        final IMapRenderState immediatelyFast$renderState = (IMapRenderState) renderState;
        if (immediatelyFast$renderState.immediatelyFast$getAtlasTexture() != null) {
            if (u == 0 && v == 1) {
                u = (float) immediatelyFast$renderState.immediatelyFast$getAtlasX() / ATLAS_SIZE;
                v = (float) (immediatelyFast$renderState.immediatelyFast$getAtlasY() + MAP_SIZE) / ATLAS_SIZE;
            } else if (u == 1 && v == 1) {
                u = (float) (immediatelyFast$renderState.immediatelyFast$getAtlasX() + MAP_SIZE) / ATLAS_SIZE;
                v = (float) (immediatelyFast$renderState.immediatelyFast$getAtlasY() + MAP_SIZE) / ATLAS_SIZE;
            } else if (u == 1 && v == 0) {
                u = (float) (immediatelyFast$renderState.immediatelyFast$getAtlasX() + MAP_SIZE) / ATLAS_SIZE;
                v = (float) immediatelyFast$renderState.immediatelyFast$getAtlasY() / ATLAS_SIZE;
            } else if (u == 0 && v == 0) {
                u = (float) immediatelyFast$renderState.immediatelyFast$getAtlasX() / ATLAS_SIZE;
                v = (float) immediatelyFast$renderState.immediatelyFast$getAtlasY() / ATLAS_SIZE;
            }
        }
        return instance.texture(u, v);
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void initAtlasParameters(MapIdComponent mapId, MapState mapState, MapRenderState renderState, CallbackInfo ci) {
        final int packedLocation = ((IMapTextureManager) this.textureManager).immediatelyFast$getAtlasMapping(mapId.id());
        if (packedLocation == -1) {
            ImmediatelyFast.LOGGER.warn("Map " + mapId.id() + " is not in an atlas");
            // Leave atlasTexture null to indicate that this map is not in an atlas, and it should use the vanilla system instead
            return;
        }
        final IMapRenderState immediatelyFast$renderState = (IMapRenderState) renderState;

        immediatelyFast$renderState.immediatelyFast$setAtlasX(((packedLocation >> 8) & 0xFF) * MAP_SIZE);
        immediatelyFast$renderState.immediatelyFast$setAtlasY((packedLocation & 0xFF) * MAP_SIZE);
        immediatelyFast$renderState.immediatelyFast$setAtlasTexture(((IMapTextureManager) this.textureManager).immediatelyFast$getMapAtlasTexture(packedLocation >> 16));
        if (immediatelyFast$renderState.immediatelyFast$getAtlasTexture() == null) {
            throw new IllegalStateException("getMapAtlasTexture returned null for packedLocation " + packedLocation + " (map " + mapId.id() + ")");
        }
    }

}
