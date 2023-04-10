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
package net.raphimc.immediatelyfast.feature.core;

public class ImmediatelyFastConfig {

    // Regular config values
    private String REGULAR_INFO = "----- Regular config values below -----";
    public boolean font_atlas_resizing = true;
    public boolean map_atlas_generation = true;
    public boolean hud_batching = true;
    public boolean fast_text_lookup = true;
    public boolean fast_buffer_upload = true;

    // Cosmetic config values
    private String COSMETIC_INFO = "----- Cosmetic only config values below (Does not optimize anything) -----";
    public boolean dont_add_info_into_debug_hud = false;

    // Experimental config values
    private String EXPERIMENTAL_INFO = "----- Experimental config values below (Rendering glitches may occur) -----";
    public boolean experimental_item_hud_batching = false;
    public boolean experimental_disable_error_checking = false;

    // Debug config values
    private String DEBUG_INFO = "----- Debug only config values below (Do not touch) -----";
    public boolean debug_only_and_not_recommended_disable_universal_batching = false;
    public boolean debug_only_and_not_recommended_disable_mod_conflict_handling = false;

}
