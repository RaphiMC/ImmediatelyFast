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

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlas;
import net.raphimc.immediatelyfast.injection.interfaces.IMapRenderState;
import net.raphimc.immediatelyfast.injection.interfaces.IMapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlas.ATLAS_SIZE;
import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlas.MAP_SIZE;

@Mixin(MapRenderer.class)
public abstract class MixinMapRenderer {

    @Shadow
    @Final
    private MapTextureManager mapTextureManager;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V", ordinal = 0))
    private SubmitNodeCollector.CustomGeometryRenderer modifyTextureCoordinates(final SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer, @Local(name = "mapRenderState", argsOnly = true) final MapRenderState mapRenderState, @Local(name = "lightCoords", argsOnly = true) final int lightCoords) {
        final IMapRenderState immediatelyFast$mapRenderState = (IMapRenderState) mapRenderState;
        if (immediatelyFast$mapRenderState.immediatelyFast$getAtlasTextureId() != null && immediatelyFast$mapRenderState.immediatelyFast$getAtlasTextureId().equals(mapRenderState.texture)) {
            final float u0 = (float) immediatelyFast$mapRenderState.immediatelyFast$getAtlasX() / ATLAS_SIZE;
            final float u1 = (float) (immediatelyFast$mapRenderState.immediatelyFast$getAtlasX() + MAP_SIZE) / ATLAS_SIZE;
            final float v0 = (float) immediatelyFast$mapRenderState.immediatelyFast$getAtlasY() / ATLAS_SIZE;
            final float v1 = (float) (immediatelyFast$mapRenderState.immediatelyFast$getAtlasY() + MAP_SIZE) / ATLAS_SIZE;
            return (matrix, vertexConsumer) -> {
                vertexConsumer.addVertex(matrix, 0F, MAP_SIZE, -0.01F).setColor(-1).setUv(u0, v1).setLight(lightCoords);
                vertexConsumer.addVertex(matrix, MAP_SIZE, MAP_SIZE, -0.01F).setColor(-1).setUv(u1, v1).setLight(lightCoords);
                vertexConsumer.addVertex(matrix, MAP_SIZE, 0F, -0.01F).setColor(-1).setUv(u1, v0).setLight(lightCoords);
                vertexConsumer.addVertex(matrix, 0F, 0F, -0.01F).setColor(-1).setUv(u0, v0).setLight(lightCoords);
            };
        } else {
            return customGeometryRenderer;
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void setAtlasParameters(final MapId mapId, final MapItemSavedData mapData, final MapRenderState mapRenderState, final CallbackInfo ci) {
        final IMapRenderState immediatelyFast$mapRenderState = (IMapRenderState) mapRenderState;
        final int location = ((IMapTextureManager) this.mapTextureManager).immediatelyFast$getAtlasLocation(mapId.id());
        if (location != -1) {
            immediatelyFast$mapRenderState.immediatelyFast$setAtlasTextureId(((IMapTextureManager) this.mapTextureManager).immediatelyFast$getAtlas(MapAtlas.getAtlasIdFromLocation(location)).getTextureId());
            immediatelyFast$mapRenderState.immediatelyFast$setAtlasX(MapAtlas.getAtlasXFromLocation(location) * MAP_SIZE);
            immediatelyFast$mapRenderState.immediatelyFast$setAtlasY(MapAtlas.getAtlasYFromLocation(location) * MAP_SIZE);
            mapRenderState.texture = immediatelyFast$mapRenderState.immediatelyFast$getAtlasTextureId();
        } else {
            immediatelyFast$mapRenderState.immediatelyFast$setAtlasTextureId(null);
            immediatelyFast$mapRenderState.immediatelyFast$setAtlasX(0);
            immediatelyFast$mapRenderState.immediatelyFast$setAtlasY(0);
        }
    }

}
