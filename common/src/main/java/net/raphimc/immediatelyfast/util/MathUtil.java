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
package net.raphimc.immediatelyfast.util;

public class MathUtil {

    private static final int DATA_BASE_UNIT = 1024;
    private static final String[] DATA_UNITS = new String[]{"KiB", "MiB", "GiB", "TiB", "PiB", "EiB"};

    /**
     * Convert a byte count to a human-readable string.
     *
     * @param bytes The byte count
     * @return The human-readable string
     */
    public static String formatBytes(long bytes) {
        final boolean negative = bytes < 0;
        bytes = Math.abs(bytes);
        if (bytes < DATA_BASE_UNIT) {
            return bytes + " B";
        } else {
            final int exponent = (int) (Math.log(bytes) / Math.log(DATA_BASE_UNIT));
            return (negative ? "-" : "") + String.format("%.1f ", bytes / Math.pow(DATA_BASE_UNIT, exponent)) + DATA_UNITS[exponent - 1];
        }
    }

}
