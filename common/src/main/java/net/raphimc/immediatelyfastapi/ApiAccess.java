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
package net.raphimc.immediatelyfastapi;

public interface ApiAccess {

    /**
     * @return An interface to access the HUD batching system of ImmediatelyFast.
     */
    @Deprecated
    BatchingAccess getBatching();

    /**
     * @return An interface to access the config of ImmediatelyFast.
     */
    ConfigAccess getConfig();

    /**
     * @return An interface to access the runtime config of ImmediatelyFast.
     */
    ConfigAccess getRuntimeConfig();

}
