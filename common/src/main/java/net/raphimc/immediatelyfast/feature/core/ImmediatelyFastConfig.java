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
package net.raphimc.immediatelyfast.feature.core;

public class ImmediatelyFastConfig {

    // Regular config values
    private String REGULAR_INFO = "----- Regular config values below -----";
    public boolean font_atlas_resizing = true;
    public boolean map_atlas_generation = true;
    public boolean fast_text_lookup = true;
    public boolean avoid_redundant_framebuffer_switching = true;
    public boolean fix_slow_buffer_upload_on_apple_gpu = true;

    // Cosmetic config values
    private String COSMETIC_INFO = "----- Cosmetic only config values below (Does not optimize anything) -----";
    public boolean dont_add_info_into_debug_hud = false;

    // Experimental config values
    private String EXPERIMENTAL_INFO = "----- Experimental config values below (Rendering glitches may occur) -----";
    public boolean experimental_disable_error_checking = false;
    public boolean experimental_disable_resource_pack_conflict_handling = false;
    public boolean experimental_sign_text_buffering = false;

    // Debug config values
    private String DEBUG_INFO = "----- Debug only config values below (Do not touch) -----";
    public boolean debug_only_and_not_recommended_disable_universal_batching = false;
    public boolean debug_only_and_not_recommended_disable_mod_conflict_handling = false;
    public boolean debug_only_and_not_recommended_disable_hardware_conflict_handling = false;
    public boolean debug_only_print_additional_error_information = false;
    public boolean debug_only_use_last_usage_for_batch_ordering = false;

    public boolean getBoolean(String key, boolean defaultValue) {
        return switch (key) {
            case "font_atlas_resizing" -> this.font_atlas_resizing;
            case "map_atlas_generation" -> this.map_atlas_generation;
            case "fast_text_lookup" -> this.fast_text_lookup;
            case "avoid_redundant_framebuffer_switching" -> this.avoid_redundant_framebuffer_switching;
            case "fix_slow_buffer_upload_on_apple_gpu" -> this.fix_slow_buffer_upload_on_apple_gpu;
            case "dont_add_info_into_debug_hud" -> this.dont_add_info_into_debug_hud;
            case "experimental_disable_error_checking" -> this.experimental_disable_error_checking;
            case "experimental_disable_resource_pack_conflict_handling" -> this.experimental_disable_resource_pack_conflict_handling;
            case "experimental_sign_text_buffering" -> this.experimental_sign_text_buffering;
            case "debug_only_and_not_recommended_disable_universal_batching" -> this.debug_only_and_not_recommended_disable_universal_batching;
            case "debug_only_and_not_recommended_disable_mod_conflict_handling" -> this.debug_only_and_not_recommended_disable_mod_conflict_handling;
            case "debug_only_and_not_recommended_disable_hardware_conflict_handling" -> this.debug_only_and_not_recommended_disable_hardware_conflict_handling;
            case "debug_only_print_additional_error_information" -> this.debug_only_print_additional_error_information;
            case "debug_only_use_last_usage_for_batch_ordering" -> this.debug_only_use_last_usage_for_batch_ordering;
            default -> defaultValue;
        };
    }

    public int getInt(String key, int defaultValue) {
        return defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        return defaultValue;
    }

}
