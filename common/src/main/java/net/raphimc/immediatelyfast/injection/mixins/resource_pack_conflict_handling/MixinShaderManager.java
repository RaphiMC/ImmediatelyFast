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
package net.raphimc.immediatelyfast.injection.mixins.resource_pack_conflict_handling;

import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastResourcePackMetadata;
import net.raphimc.immediatelyfast.feature.resource_pack_conflict_handling.CoreShaderBlacklist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Mixin(ShaderManager.class)
public abstract class MixinShaderManager {

    @Inject(method = "apply(Lnet/minecraft/client/renderer/ShaderManager$Configs;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
    private void checkForCoreShaderModifications(ShaderManager.Configs configs, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci) {
        PackResources resourcePackWhichBreaksFontAtlasResizing = null;
        try {
            final Set<PackResources> breakingResourcePacks = new HashSet<>();
            for (ResourceLocation shaderIdentifier : CoreShaderBlacklist.getBlacklist()) {
                final ResourceLocation vertexShaderIdentifier = ShaderType.VERTEX.idConverter().idToFile(shaderIdentifier);
                final PackResources vertexShaderResourcePack = resourceManager.getResource(vertexShaderIdentifier).map(Resource::source).orElse(null);
                if (vertexShaderResourcePack != null && !vertexShaderResourcePack.equals(Minecraft.getInstance().getVanillaPackResources())) {
                    breakingResourcePacks.add(vertexShaderResourcePack);
                }
                final ResourceLocation fragmentShaderIdentifier = ShaderType.FRAGMENT.idConverter().idToFile(shaderIdentifier);
                final PackResources fragmentShaderResourcePack = resourceManager.getResource(fragmentShaderIdentifier).map(Resource::source).orElse(null);
                if (fragmentShaderResourcePack != null && !fragmentShaderResourcePack.equals(Minecraft.getInstance().getVanillaPackResources())) {
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
            }
        } catch (IOException e) {
            ImmediatelyFast.LOGGER.error("Failed to check for core shader modifications", e);
        }

        if (ImmediatelyFast.runtimeConfig.font_atlas_resizing && resourcePackWhichBreaksFontAtlasResizing != null) {
            ImmediatelyFast.LOGGER.warn("Resource pack " + resourcePackWhichBreaksFontAtlasResizing.packId() + " is not compatible with font atlas resizing. Temporarily disabling font atlas resizing.");
            ImmediatelyFast.runtimeConfig.font_atlas_resizing = false;
            this.immediatelyFast$reloadFontStorages();
        } else {
            if (!ImmediatelyFast.runtimeConfig.font_atlas_resizing && ImmediatelyFast.config.font_atlas_resizing) {
                ImmediatelyFast.runtimeConfig.font_atlas_resizing = true;
                this.immediatelyFast$reloadFontStorages();
            }
        }
    }

    @Unique
    private void immediatelyFast$reloadFontStorages() {
        // Force reload the font manager to rebuild the font atlas textures
        Minecraft.getInstance().fontManager.updateOptions(Minecraft.getInstance().options);
    }

}
