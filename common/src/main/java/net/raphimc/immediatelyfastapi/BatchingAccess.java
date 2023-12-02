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
package net.raphimc.immediatelyfastapi;

public interface BatchingAccess {

    /**
     * Starts batching all rendered HUD elements between {@link #beginHudBatching()} and {@link #endHudBatching()}.<br>
     * Make sure to <b>always</b> call {@link #endHudBatching()} after you began batching when rendering your HUD elements even if you didn't render anything.
     */
    void beginHudBatching();

    /**
     * Draws all batched HUD elements at once.
     */
    void endHudBatching();

    /**
     * @return Whether HUD batching is currently active.
     */
    boolean isHudBatching();

}
