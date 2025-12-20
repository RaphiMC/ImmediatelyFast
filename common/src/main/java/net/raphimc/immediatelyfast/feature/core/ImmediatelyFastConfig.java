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
    public boolean enhanced_batching = true;
    public boolean font_atlas_resizing = true;
    public int font_atlas_size = 1024;
    public boolean map_atlas_generation = true;
    public int map_atlas_size = 2048;
    public boolean skip_text_translucency_sorting = true;
    public boolean fast_text_lookup = true;
    public boolean avoid_redundant_framebuffer_switching = true;
    public boolean fix_slow_buffer_upload_on_apple_gpu = true;

    // Experimental config values
    private String EXPERIMENTAL_INFO = "----- Experimental config values below (Rendering glitches may occur) -----";
    public boolean experimental_disable_resource_pack_conflict_handling = false;
    public boolean experimental_sign_text_buffering = false;

    // Debug config values
    private String DEBUG_INFO = "----- Debug only config values below (Do not touch) -----";
    public boolean debug_only_and_not_recommended_disable_mod_conflict_handling = false;
    public boolean debug_only_and_not_recommended_disable_hardware_conflict_handling = false;
    public boolean debug_only_print_additional_error_information = false;
    public boolean debug_only_use_last_usage_for_batch_ordering = false;
    public boolean debug_only_detailed_memory_leak_detection = false;

    public boolean getBoolean(final String key, final boolean defaultValue) {
        return switch (key) {
            case "enhanced_batching" -> this.enhanced_batching;
            case "font_atlas_resizing" -> this.font_atlas_resizing;
            case "map_atlas_generation" -> this.map_atlas_generation;
            case "skip_text_translucency_sorting" -> this.skip_text_translucency_sorting;
            case "fast_text_lookup" -> this.fast_text_lookup;
            case "avoid_redundant_framebuffer_switching" -> this.avoid_redundant_framebuffer_switching;
            case "fix_slow_buffer_upload_on_apple_gpu" -> this.fix_slow_buffer_upload_on_apple_gpu;
            case "experimental_disable_resource_pack_conflict_handling" -> this.experimental_disable_resource_pack_conflict_handling;
            case "experimental_sign_text_buffering" -> this.experimental_sign_text_buffering;
            case "debug_only_and_not_recommended_disable_mod_conflict_handling" -> this.debug_only_and_not_recommended_disable_mod_conflict_handling;
            case "debug_only_and_not_recommended_disable_hardware_conflict_handling" -> this.debug_only_and_not_recommended_disable_hardware_conflict_handling;
            case "debug_only_print_additional_error_information" -> this.debug_only_print_additional_error_information;
            case "debug_only_use_last_usage_for_batch_ordering" -> this.debug_only_use_last_usage_for_batch_ordering;
            case "debug_only_detailed_memory_leak_detection" -> this.debug_only_detailed_memory_leak_detection;
            default -> defaultValue;
        };
    }

    public int getInt(final String key, final int defaultValue) {
        return switch (key) {
            case "font_atlas_size" -> this.font_atlas_size;
            case "map_atlas_size" -> this.map_atlas_size;
            default -> defaultValue;
        };
    }

    public long getLong(final String key, final long defaultValue) {
        return defaultValue;
    }

    public String getString(final String key, final String defaultValue) {
        return defaultValue;
    }

}
