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
package net.raphimc.immediatelyfast.compat;

import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;

import java.util.List;

public class CoreShaderBlacklist {

    private static final List<ShaderProgramKey> BLACKLIST = List.of(
            ShaderProgramKeys.POSITION_COLOR,
            ShaderProgramKeys.POSITION_TEX,
            ShaderProgramKeys.POSITION_TEX_COLOR,
            ShaderProgramKeys.RENDERTYPE_TEXT,
            ShaderProgramKeys.RENDERTYPE_TEXT_BACKGROUND,
            ShaderProgramKeys.RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH,
            ShaderProgramKeys.RENDERTYPE_TEXT_INTENSITY,
            ShaderProgramKeys.RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH,
            ShaderProgramKeys.RENDERTYPE_TEXT_SEE_THROUGH,
            ShaderProgramKeys.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL
    );

    public static List<ShaderProgramKey> getBlacklist() {
        return BLACKLIST;
    }

}
