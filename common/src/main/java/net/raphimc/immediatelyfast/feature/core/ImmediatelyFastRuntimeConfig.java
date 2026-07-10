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
package net.raphimc.immediatelyfast.feature.core;

public class ImmediatelyFastRuntimeConfig {

    public boolean font_atlas_resizing;
    public boolean map_atlas_generation;
    public boolean disable_fast_buffer_upload;

    public ImmediatelyFastRuntimeConfig(final ImmediatelyFastConfig config) {
        this.font_atlas_resizing = config.font_atlas_resizing;
        this.map_atlas_generation = config.map_atlas_generation;
        this.disable_fast_buffer_upload = false;
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        return switch (key) {
            case "font_atlas_resizing" -> this.font_atlas_resizing;
            case "map_atlas_generation" -> this.map_atlas_generation;
            case "disable_fast_buffer_upload" -> this.disable_fast_buffer_upload;
            default -> defaultValue;
        };
    }

    public int getInt(final String key, final int defaultValue) {
        return defaultValue;
    }

    public long getLong(final String key, final long defaultValue) {
        return defaultValue;
    }

    public String getString(final String key, final String defaultValue) {
        return defaultValue;
    }

}
