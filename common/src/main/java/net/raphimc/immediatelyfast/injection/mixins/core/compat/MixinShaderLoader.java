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
package net.raphimc.immediatelyfast.injection.mixins.core.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.gl.ShaderProgramDefinition;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.compat.CoreShaderBlacklist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ShaderLoader.class)
public abstract class MixinShaderLoader {

    @Inject(method = "apply(Lnet/minecraft/client/gl/ShaderLoader$Definitions;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("RETURN"))
    private void checkForCoreShaderModifications(ShaderLoader.Definitions definitions, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        boolean modified = false;

        for (Map.Entry<Identifier, ShaderProgramDefinition> entry : definitions.programs().entrySet()) {
            if (!CoreShaderBlacklist.isBlacklisted(entry.getKey())) continue;

            final Resource resource = resourceManager.getResource(entry.getValue().vertex()).orElse(null);
            if (resource != null && !resource.getPack().equals(MinecraftClient.getInstance().getDefaultResourcePack())) {
                modified = true;
                break;
            }
        }

        if (modified && !ImmediatelyFast.config.experimental_disable_resource_pack_conflict_handling) {
            ImmediatelyFast.LOGGER.warn("Core shader modifications detected. Temporarily disabling some parts of ImmediatelyFast.");
            if (ImmediatelyFast.runtimeConfig.font_atlas_resizing) {
                ImmediatelyFast.runtimeConfig.font_atlas_resizing = false;
                this.immediatelyFast$reloadFontStorages();
            }

            ImmediatelyFast.runtimeConfig.hud_batching = false;
            ImmediatelyFast.runtimeConfig.experimental_screen_batching = false;
        } else {
            if (!ImmediatelyFast.runtimeConfig.font_atlas_resizing && ImmediatelyFast.config.font_atlas_resizing) {
                ImmediatelyFast.runtimeConfig.font_atlas_resizing = true;
                this.immediatelyFast$reloadFontStorages();
            }

            ImmediatelyFast.runtimeConfig.hud_batching = ImmediatelyFast.config.hud_batching;
            ImmediatelyFast.runtimeConfig.experimental_screen_batching = ImmediatelyFast.config.experimental_screen_batching;
        }
    }

    @Unique
    private void immediatelyFast$reloadFontStorages() {
        // Force reload the font storages to rebuild the font atlas textures
        MinecraftClient.getInstance().fontManager.setActiveFilters(MinecraftClient.getInstance().options);
    }

}
