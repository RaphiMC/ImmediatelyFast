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
package net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.injection.interfaces.IMapRenderState;
import net.raphimc.immediatelyfast.injection.interfaces.IMapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture.ATLAS_SIZE;
import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture.MAP_SIZE;

@Mixin(MapRenderer.class)
public abstract class MixinMapRenderer {

    @Shadow
    @Final
    private MapTextureManager mapTextureManager;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V", ordinal = 0))
    private SubmitNodeCollector.CustomGeometryRenderer modifyTextureCoordinates(SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer, @Local(argsOnly = true) MapRenderState renderState, @Local(argsOnly = true) int light) {
        final IMapRenderState immediatelyFast$renderState = (IMapRenderState) renderState;
        if (immediatelyFast$renderState.immediatelyFast$getAtlasTexture() != null) {
            final float u1 = (float) immediatelyFast$renderState.immediatelyFast$getAtlasX() / ATLAS_SIZE;
            final float u2 = (float) (immediatelyFast$renderState.immediatelyFast$getAtlasX() + MAP_SIZE) / ATLAS_SIZE;
            final float v1 = (float) immediatelyFast$renderState.immediatelyFast$getAtlasY() / ATLAS_SIZE;
            final float v2 = (float) (immediatelyFast$renderState.immediatelyFast$getAtlasY() + MAP_SIZE) / ATLAS_SIZE;
            return (matrix, vertexConsumer) -> {
                vertexConsumer.addVertex(matrix, 0F, MAP_SIZE, -0.01F).setColor(-1).setUv(u1, v2).setLight(light);
                vertexConsumer.addVertex(matrix, MAP_SIZE, MAP_SIZE, -0.01F).setColor(-1).setUv(u2, v2).setLight(light);
                vertexConsumer.addVertex(matrix, MAP_SIZE, 0F, -0.01F).setColor(-1).setUv(u2, v1).setLight(light);
                vertexConsumer.addVertex(matrix, 0F, 0F, -0.01F).setColor(-1).setUv(u1, v1).setLight(light);
            };
        } else {
            return customGeometryRenderer;
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void initAtlasParameters(MapId mapId, MapItemSavedData mapState, MapRenderState renderState, CallbackInfo ci) {
        final int packedLocation = ((IMapTextureManager) this.mapTextureManager).immediatelyFast$getAtlasMapping(mapId.id());
        if (packedLocation == -1) {
            ImmediatelyFast.LOGGER.warn("Map " + mapId.id() + " is not in an atlas");
            // Leave atlasTexture null to indicate that this map is not in an atlas, and it should use the vanilla system instead
            return;
        }

        final IMapRenderState immediatelyFast$renderState = (IMapRenderState) renderState;
        immediatelyFast$renderState.immediatelyFast$setAtlasX(((packedLocation >> 8) & 0xFF) * MAP_SIZE);
        immediatelyFast$renderState.immediatelyFast$setAtlasY((packedLocation & 0xFF) * MAP_SIZE);
        immediatelyFast$renderState.immediatelyFast$setAtlasTexture(((IMapTextureManager) this.mapTextureManager).immediatelyFast$getMapAtlasTexture(packedLocation >> 16));
        if (immediatelyFast$renderState.immediatelyFast$getAtlasTexture() == null) {
            throw new IllegalStateException("getMapAtlasTexture returned null for packedLocation " + packedLocation + " (map " + mapId.id() + ")");
        }
    }

}
