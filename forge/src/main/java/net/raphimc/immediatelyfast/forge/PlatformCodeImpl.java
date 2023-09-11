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
package net.raphimc.immediatelyfast.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.PlatformCode;

import java.nio.file.Path;
import java.util.Optional;

public class PlatformCodeImpl {

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static Optional<String> getModVersion(final String mod) {
        if (ModList.get() == null) {
            return Optional.ofNullable(FMLLoader.getLoadingModList().getModFileById(mod)).map(ModFileInfo::versionString);
        }

        return ModList.get().getModContainerById(mod).map(m -> m.getModInfo().getVersion().toString());
    }

    public static void checkModCompatibility() {
        if (!ImmediatelyFast.config.debug_only_and_not_recommended_disable_mod_conflict_handling) {
            PlatformCode.getModVersion("optifine").ifPresent(version -> {
                throw new IllegalStateException("Found OptiFine " + version + ". ImmediatelyFast is not compatible with OptiFine.");
            });
        }
    }

}
