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
package net.raphimc.immediatelyfast.injection.mixins.resource_pack_conflict_handling;

import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.resource_pack_conflict_handling.CoreShaderBlacklist;
import net.raphimc.immediatelyfast.feature.resource_pack_conflict_handling.ImmediatelyFastResourcePackMetadata;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Shadow
    public abstract VanillaPackResources getVanillaPackResources();

    @Shadow
    @Final
    private FontManager fontManager;

    @Shadow
    @Final
    public Options options;

    @Shadow
    public abstract MapTextureManager getMapTextureManager();

    @Inject(method = "onResourceLoadFinished", at = @At("RETURN"))
    private void checkResourcePackCompatibility(CallbackInfo ci) {
        PackResources resourcePackWhichBreaksFontAtlasResizing = null;
        PackResources resourcePackWhichBreaksMapAtlasGeneration = null;
        try {
            final Set<PackResources> breakingResourcePacks = new HashSet<>();
            for (Identifier shaderIdentifier : CoreShaderBlacklist.getBlacklist()) {
                final Identifier vertexShaderIdentifier = ShaderType.VERTEX.idConverter().idToFile(shaderIdentifier);
                final PackResources vertexShaderResourcePack = this.resourceManager.getResource(vertexShaderIdentifier).map(Resource::source).orElse(null);
                if (vertexShaderResourcePack != null && !immediatelyFast$arePackResourcesEqual(vertexShaderResourcePack, this.getVanillaPackResources().fullResources())) {
                    breakingResourcePacks.add(vertexShaderResourcePack);
                }
                final Identifier fragmentShaderIdentifier = ShaderType.FRAGMENT.idConverter().idToFile(shaderIdentifier);
                final PackResources fragmentShaderResourcePack = this.resourceManager.getResource(fragmentShaderIdentifier).map(Resource::source).orElse(null);
                if (fragmentShaderResourcePack != null && !immediatelyFast$arePackResourcesEqual(fragmentShaderResourcePack, this.getVanillaPackResources().fullResources())) {
                    breakingResourcePacks.add(fragmentShaderResourcePack);
                }
            }
            for (PackResources resourcePack : breakingResourcePacks) {
                ImmediatelyFastResourcePackMetadata metadata = resourcePack.getMetadataSection(ImmediatelyFastResourcePackMetadata.SERIALIZER);
                if (metadata == null) {
                    metadata = ImmediatelyFastResourcePackMetadata.DEFAULT;
                }
                if (!metadata.compatibleFeatures().contains("font_atlas_resizing")) {
                    resourcePackWhichBreaksFontAtlasResizing = resourcePack;
                }
                if (metadata.incompatibleFeatures().contains("map_atlas_generation")) {
                    resourcePackWhichBreaksMapAtlasGeneration = resourcePack;
                }
            }
        } catch (IOException e) {
            ImmediatelyFast.LOGGER.error("Failed to check for core shader modifications", e);
        }

        if (ImmediatelyFast.config.font_atlas_resizing) {
            if (resourcePackWhichBreaksFontAtlasResizing != null) {
                ImmediatelyFast.LOGGER.warn("Resource pack " + resourcePackWhichBreaksFontAtlasResizing.packId() + " is not compatible with font atlas resizing. Temporarily disabling font atlas resizing.");
                if (ImmediatelyFast.runtimeConfig.font_atlas_resizing) {
                    ImmediatelyFast.runtimeConfig.font_atlas_resizing = false;
                    this.immediatelyFast$reloadFontStorages();
                }
            } else if (!ImmediatelyFast.runtimeConfig.font_atlas_resizing) {
                ImmediatelyFast.LOGGER.info("Re-enabling font atlas resizing because no incompatible resource packs are loaded.");
                ImmediatelyFast.runtimeConfig.font_atlas_resizing = true;
                this.immediatelyFast$reloadFontStorages();
            }
        }
        if (ImmediatelyFast.config.map_atlas_generation) {
            if (resourcePackWhichBreaksMapAtlasGeneration != null) {
                ImmediatelyFast.LOGGER.warn("Resource pack " + resourcePackWhichBreaksMapAtlasGeneration.packId() + " is not compatible with map atlas generation. Temporarily disabling map atlas generation.");
                if (ImmediatelyFast.runtimeConfig.map_atlas_generation) {
                    ImmediatelyFast.runtimeConfig.map_atlas_generation = false;
                    this.immediatelyFast$reloadMapTextures();
                }
            } else if (!ImmediatelyFast.runtimeConfig.map_atlas_generation) {
                ImmediatelyFast.LOGGER.info("Re-enabling map atlas generation because no incompatible resource packs are loaded.");
                ImmediatelyFast.runtimeConfig.map_atlas_generation = true;
                this.immediatelyFast$reloadMapTextures();
            }
        }
    }

    @Unique
    private void immediatelyFast$reloadFontStorages() {
        this.fontManager.updateOptions(this.options); // Force reload the font manager to rebuild the font atlas textures
    }

    @Unique
    private void immediatelyFast$reloadMapTextures() {
        this.getMapTextureManager().resetData(); // Force reset the map texture manager
    }

    @Unique
    private static boolean immediatelyFast$arePackResourcesEqual(final PackResources a, final PackResources b) {
        return a.location().equals(b.location());
    }

}
