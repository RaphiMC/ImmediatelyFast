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
package net.raphimc.immediatelyfast.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.PlatformCode;
import net.raphimc.immediatelyfast.compat.IrisCompat;

public class ImmediatelyFastFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ImmediatelyFast.modInit();

        if (!ImmediatelyFast.config.debug_only_and_not_recommended_disable_mod_conflict_handling) {
            PlatformCode.getModVersion("iris").ifPresent(version -> {
                ImmediatelyFast.LOGGER.info("Found Iris " + version + ". Enabling compatibility.");
                IrisCompat.init();
            });
        }
    }

}
