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
package net.raphimc.immediatelyfast.injection.mixins.core.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.compat.CoreShaderBlacklist;
import net.raphimc.immediatelyfast.feature.core.ImmediatelyFastResourcePackMetadata;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.*;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow
    @Final
    private Map<String, ShaderProgram> programs;

    @Inject(method = "loadPrograms", at = @At("RETURN"))
    private void checkForCoreShaderModifications(ResourceFactory factory, CallbackInfo ci) {
        if (ImmediatelyFast.config.experimental_disable_resource_pack_conflict_handling) {
            return;
        }

        ResourcePack resourcePackWhichBreaksFontAtlasResizing = null;
        ResourcePack resourcePackWhichBreaksHudBatching = null;
        try {
            final Set<ResourcePack> breakingResourcePacks = new HashSet<>();
            for (Map.Entry<String, ShaderProgram> shaderProgramEntry : this.programs.entrySet()) {
                if (!CoreShaderBlacklist.isBlacklisted(shaderProgramEntry.getKey())) continue;

                final Identifier vertexShaderIdentifier = new Identifier("shaders/core/" + shaderProgramEntry.getValue().getVertexShader().getName() + ".vsh");
                final ResourcePack vertexShaderResourcePack = factory.getResource(vertexShaderIdentifier).map(Resource::getPack).orElse(null);
                if (vertexShaderResourcePack != null && !vertexShaderResourcePack.equals(MinecraftClient.getInstance().getDefaultResourcePack())) {
                    breakingResourcePacks.add(vertexShaderResourcePack);
                }
                final Identifier fragmentShaderIdentifier = new Identifier("shaders/core/" + shaderProgramEntry.getValue().getFragmentShader().getName() + ".fsh");
                final ResourcePack fragmentShaderResourcePack = factory.getResource(fragmentShaderIdentifier).map(Resource::getPack).orElse(null);
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
                if (!metadata.compatibleFeatures().contains("hud_batching")) {
                    resourcePackWhichBreaksHudBatching = resourcePack;
                }
            }
        } catch (IOException e) {
            ImmediatelyFast.LOGGER.error("Failed to check for core shader modifications", e);
        }

        if (ImmediatelyFast.runtimeConfig.font_atlas_resizing && resourcePackWhichBreaksFontAtlasResizing != null) {
            ImmediatelyFast.LOGGER.warn("Resource pack " + resourcePackWhichBreaksFontAtlasResizing.getName() + " is not compatible with font atlas resizing. Temporarily disabling font atlas resizing.");
            ImmediatelyFast.runtimeConfig.font_atlas_resizing = false;
            this.immediatelyFast$reloadFontStorages();
        } else {
            if (!ImmediatelyFast.runtimeConfig.font_atlas_resizing && ImmediatelyFast.config.font_atlas_resizing) {
                ImmediatelyFast.runtimeConfig.font_atlas_resizing = true;
                this.immediatelyFast$reloadFontStorages();
            }
        }
        if (ImmediatelyFast.runtimeConfig.hud_batching && resourcePackWhichBreaksHudBatching != null) {
            ImmediatelyFast.LOGGER.warn("Resource pack " + resourcePackWhichBreaksHudBatching.getName() + " is not compatible with HUD batching. Temporarily disabling HUD batching.");
            ImmediatelyFast.runtimeConfig.hud_batching = false;
        } else {
            ImmediatelyFast.runtimeConfig.hud_batching = ImmediatelyFast.config.hud_batching;
        }
    }

    @Unique
    private void immediatelyFast$reloadFontStorages() {
        // Force reload the font storages to rebuild the font atlas textures
        for (FontStorage storage : MinecraftClient.getInstance().fontManager.fontStorages.values()) {
            final List<Font> fonts = new ArrayList<>(storage.fonts);
            storage.fonts.clear();
            storage.setFonts(fonts);
        }
    }

}
