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
package net.raphimc.immediatelyfast.feature.resource_pack_conflict_handling;

import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Set;

public class CoreShaderBlacklist {

    private static final Set<Identifier> BLACKLIST = Set.of(
            Identifier.withDefaultNamespace("core/position_color"),
            Identifier.withDefaultNamespace("core/position_tex"),
            Identifier.withDefaultNamespace("core/position_tex_color"),
            Identifier.withDefaultNamespace("core/rendertype_text"),
            Identifier.withDefaultNamespace("core/rendertype_text_background"),
            Identifier.withDefaultNamespace("core/rendertype_text_background_see_through"),
            Identifier.withDefaultNamespace("core/rendertype_text_intensity"),
            Identifier.withDefaultNamespace("core/rendertype_text_intensity_see_through"),
            Identifier.withDefaultNamespace("core/rendertype_text_see_through"),
            Identifier.withDefaultNamespace("core/rendertype_item_entity_translucent_cull")
    );

    static {
        if (false /* Set to true if updating the game version to validate the identifiers */) {
            final ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            for (Identifier shaderIdentifier : BLACKLIST) {
                final Resource vertexShaderResource = resourceManager.getResource(ShaderType.VERTEX.idConverter().idToFile(shaderIdentifier)).orElse(null);
                if (vertexShaderResource == null) {
                    throw new RuntimeException("Couldn't find vertex shader " + shaderIdentifier);
                }
                final Resource fragmentShaderResource = resourceManager.getResource(ShaderType.FRAGMENT.idConverter().idToFile(shaderIdentifier)).orElse(null);
                if (fragmentShaderResource == null) {
                    throw new RuntimeException("Couldn't find fragment shader " + shaderIdentifier);
                }
            }
        }
    }

    public static Set<Identifier> getBlacklist() {
        return BLACKLIST;
    }

}
