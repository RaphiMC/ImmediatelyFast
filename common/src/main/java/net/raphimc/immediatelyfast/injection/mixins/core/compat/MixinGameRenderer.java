/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
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
import net.minecraft.util.Identifier;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow
    @Final
    MinecraftClient client;

    @Shadow
    @Final
    private Map<String, ShaderProgram> programs;

    @Unique
    private final List<String> immediatelyFast$cantBeModified = List.of(
            "rendertype_text",
            "rendertype_text_background",
            "rendertype_text_background_see_through",
            "rendertype_text_intensity",
            "rendertype_text_intensity_see_through",
            "rendertype_text_see_through",
            "rendertype_entity_translucent_cull",
            "rendertype_item_entity_translucent_cull"
    );

    @Inject(method = "loadPrograms", at = @At("RETURN"))
    private void checkForCoreShaderModifications(ResourceFactory factory, CallbackInfo ci) {
        boolean modified = false;
        for (Map.Entry<String, ShaderProgram> shaderProgramEntry : this.programs.entrySet()) {
            if (!this.immediatelyFast$cantBeModified.contains(shaderProgramEntry.getKey())) continue;

            final Identifier vertexIdentifier = new Identifier("shaders/core/" + shaderProgramEntry.getValue().getVertexShader().getName() + ".vsh");
            final Resource resource = factory.getResource(vertexIdentifier).orElse(null);
            if (resource != null && !resource.getPack().equals(this.client.getDefaultResourcePack())) {
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
        } else {
            if (!ImmediatelyFast.runtimeConfig.font_atlas_resizing && ImmediatelyFast.config.font_atlas_resizing) {
                ImmediatelyFast.runtimeConfig.font_atlas_resizing = true;
                this.immediatelyFast$reloadFontStorages();
            }

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
