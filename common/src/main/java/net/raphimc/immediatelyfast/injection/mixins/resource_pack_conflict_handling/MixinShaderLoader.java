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

import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
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

@Mixin(ShaderLoader.class)
public abstract class MixinShaderLoader {

    @Inject(method = "apply(Lnet/minecraft/client/gl/ShaderLoader$Definitions;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("RETURN"))
    private void checkForCoreShaderModifications(ShaderLoader.Definitions definitions, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        ResourcePack resourcePackWhichBreaksFontAtlasResizing = null;
        try {
            final Set<ResourcePack> breakingResourcePacks = new HashSet<>();
            for (Identifier shaderIdentifier : CoreShaderBlacklist.getBlacklist()) {
                final Identifier vertexShaderIdentifier = ShaderType.VERTEX.idConverter().toResourcePath(shaderIdentifier);
                final ResourcePack vertexShaderResourcePack = resourceManager.getResource(vertexShaderIdentifier).map(Resource::getPack).orElse(null);
                if (vertexShaderResourcePack != null && !vertexShaderResourcePack.equals(MinecraftClient.getInstance().getDefaultResourcePack())) {
                    breakingResourcePacks.add(vertexShaderResourcePack);
                }
                final Identifier fragmentShaderIdentifier = ShaderType.FRAGMENT.idConverter().toResourcePath(shaderIdentifier);
                final ResourcePack fragmentShaderResourcePack = resourceManager.getResource(fragmentShaderIdentifier).map(Resource::getPack).orElse(null);
                if (fragmentShaderResourcePack != null && !fragmentShaderResourcePack.equals(MinecraftClient.getInstance().getDefaultResourcePack())) {
                    breakingResourcePacks.add(fragmentShaderResourcePack);
                }
            }
            for (ResourcePack resourcePack : breakingResourcePacks) {
                ImmediatelyFastResourcePackMetadata metadata = resourcePack.parseMetadata(ImmediatelyFastResourcePackMetadata.SERIALIZER);
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

        if (ImmediatelyFast.config.font_atlas_resizing) {
            if (resourcePackWhichBreaksFontAtlasResizing != null) {
                ImmediatelyFast.LOGGER.warn("Resource pack " + resourcePackWhichBreaksFontAtlasResizing.getId() + " is not compatible with font atlas resizing. Temporarily disabling font atlas resizing.");
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
    }

    @Unique
    private void immediatelyFast$reloadFontStorages() {
        // Force reload the font storages to rebuild the font atlas textures
        MinecraftClient.getInstance().fontManager.setActiveFilters(MinecraftClient.getInstance().options);
    }

}
