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
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.lenni0451.reflect.Objects;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture;
import net.raphimc.immediatelyfast.injection.interfaces.IMapTextureManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

import static net.raphimc.immediatelyfast.feature.map_atlas_generation.MapAtlasTexture.MAP_SIZE;

@Mixin(MapTextureManager.MapInstance.class)
public abstract class MixinMapTextureManager_MapInstance {

    @Shadow
    private MapItemSavedData data;

    @Mutable
    @Shadow
    @Final
    private DynamicTexture texture;

    @Shadow
    private boolean requiresUpload;

    @Shadow
    @Final
    @Mutable
    Identifier location;

    @Unique
    private static final DynamicTexture DUMMY_TEXTURE = Objects.allocate(DynamicTexture.class);

    @Unique
    private int immediatelyFast$atlasX;

    @Unique
    private int immediatelyFast$atlasY;

    @Unique
    private MapAtlasTexture immediatelyFast$atlasTexture;

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Ljava/util/function/Supplier;IIZ)Lnet/minecraft/client/renderer/texture/DynamicTexture;"))
    private DynamicTexture initAtlasParametersAndDontAllocateTexture(Supplier<String> label, int width, int height, boolean useCalloc, @Local(argsOnly = true) MapTextureManager mapTextureManager, @Local(argsOnly = true) int id) {
        final int packedLocation = ((IMapTextureManager) mapTextureManager).immediatelyFast$getAtlasMapping(id);
        if (packedLocation == -1) {
            ImmediatelyFast.LOGGER.warn("Map " + id + " is not in an atlas");
            // Leave atlasTexture null to indicate that this map is not in an atlas, and it should use the vanilla system instead
            return new DynamicTexture(label, width, height, useCalloc);
        }

        this.immediatelyFast$atlasX = ((packedLocation >> 8) & 0xFF) * MAP_SIZE;
        this.immediatelyFast$atlasY = (packedLocation & 0xFF) * MAP_SIZE;
        this.immediatelyFast$atlasTexture = ((IMapTextureManager) mapTextureManager).immediatelyFast$getMapAtlasTexture(packedLocation >> 16);
        if (this.immediatelyFast$atlasTexture == null) {
            throw new IllegalStateException("getMapAtlasTexture returned null for packedLocation " + packedLocation + " (map " + id + ")");
        }

        return DUMMY_TEXTURE;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V"))
    private void getAtlasTextureIdentifier(TextureManager instance, Identifier path, AbstractTexture texture) {
        if (this.immediatelyFast$atlasTexture != null) {
            this.texture = null; // Don't leave the texture field pointing to the uninitialized dummy texture
            this.location = this.immediatelyFast$atlasTexture.getTextureId();
        } else {
            instance.register(path, texture);
        }
    }

    @Inject(method = "updateTextureIfNeeded", at = @At("HEAD"), cancellable = true)
    private void updateAtlasTexture(CallbackInfo ci) {
        if (this.requiresUpload && this.immediatelyFast$atlasTexture != null) {
            ci.cancel();
            final DynamicTexture atlasTexture = this.immediatelyFast$atlasTexture.getTexture();
            final NativeImage atlasImage = atlasTexture.getPixels();
            if (atlasImage == null) {
                throw new IllegalStateException("Atlas texture has already been closed");
            }

            for (int x = 0; x < MAP_SIZE; x++) {
                for (int y = 0; y < MAP_SIZE; y++) {
                    final int i = x + y * MAP_SIZE;
                    atlasImage.setPixel(this.immediatelyFast$atlasX + x, this.immediatelyFast$atlasY + y, MapColor.getColorFromPackedId(this.data.colors[i]));
                }
            }
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(atlasTexture.getTexture(), atlasImage, 0, 0, this.immediatelyFast$atlasX, this.immediatelyFast$atlasY, MAP_SIZE, MAP_SIZE, this.immediatelyFast$atlasX, this.immediatelyFast$atlasY);
            this.requiresUpload = false;
        }
    }

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    private void dontCloseDummyTexture(CallbackInfo ci) {
        if (this.immediatelyFast$atlasTexture != null) {
            ci.cancel();
        }
    }

}
