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

public class RuntimeConfigAccessImpl implements ConfigAccess {

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return switch (key) {
            case "font_atlas_resizing" -> ImmediatelyFast.runtimeConfig.font_atlas_resizing;
            case "hud_batching" -> ImmediatelyFast.runtimeConfig.hud_batching;
            case "experimental_screen_batching" -> ImmediatelyFast.runtimeConfig.experimental_screen_batching;
            default -> defaultValue;
        };
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return defaultValue;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return defaultValue;
    }

    @Override
    public String getString(String key, String defaultValue) {
        return defaultValue;
    }

}
