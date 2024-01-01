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
package net.raphimc.immediatelyfast.apiimpl;

import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfastapi.ConfigAccess;

public class ConfigAccessImpl implements ConfigAccess {

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return switch (key) {
            case "font_atlas_resizing" -> ImmediatelyFast.config.font_atlas_resizing;
            case "map_atlas_generation" -> ImmediatelyFast.config.map_atlas_generation;
            case "hud_batching" -> ImmediatelyFast.config.hud_batching;
            case "fast_text_lookup" -> ImmediatelyFast.config.fast_text_lookup;
            case "fast_buffer_upload" -> ImmediatelyFast.config.fast_buffer_upload;
            case "fast_buffer_upload_explicit_flush" -> ImmediatelyFast.config.fast_buffer_upload_explicit_flush;
            case "dont_add_info_into_debug_hud" -> ImmediatelyFast.config.dont_add_info_into_debug_hud;
            case "experimental_disable_error_checking" -> ImmediatelyFast.config.experimental_disable_error_checking;
            case "experimental_disable_resource_pack_conflict_handling" -> ImmediatelyFast.config.experimental_disable_resource_pack_conflict_handling;
            case "experimental_screen_batching" -> ImmediatelyFast.config.experimental_screen_batching;
            case "debug_only_and_not_recommended_disable_universal_batching" -> ImmediatelyFast.config.debug_only_and_not_recommended_disable_universal_batching;
            case "debug_only_and_not_recommended_disable_mod_conflict_handling" -> ImmediatelyFast.config.debug_only_and_not_recommended_disable_mod_conflict_handling;
            case "debug_only_and_not_recommended_disable_hardware_conflict_handling" -> ImmediatelyFast.config.debug_only_and_not_recommended_disable_hardware_conflict_handling;
            case "debug_only_print_additional_error_information" -> ImmediatelyFast.config.debug_only_print_additional_error_information;
            default -> defaultValue;
        };
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return defaultValue;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        if (key.equals("fast_buffer_upload_size_mb")) {
            return ImmediatelyFast.config.fast_buffer_upload_size_mb;
        }
        return defaultValue;
    }

    @Override
    public String getString(String key, String defaultValue) {
        return defaultValue;
    }

}
