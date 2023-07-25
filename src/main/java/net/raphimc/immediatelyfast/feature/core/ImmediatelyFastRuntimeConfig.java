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

public class ImmediatelyFastRuntimeConfig {

    public boolean hud_batching;
    public boolean font_atlas_resizing;
    public boolean fast_buffer_upload;
    public boolean universal_batching_text;
    public boolean legacy_fast_buffer_upload;

    public ImmediatelyFastRuntimeConfig(final ImmediatelyFastConfig config) {
        this.hud_batching = config.hud_batching;
        this.font_atlas_resizing = config.font_atlas_resizing;
        this.fast_buffer_upload = config.fast_buffer_upload;
        this.universal_batching_text = true;
        this.legacy_fast_buffer_upload = false;
    }

}
