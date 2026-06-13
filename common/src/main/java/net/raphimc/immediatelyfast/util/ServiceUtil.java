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

import java.util.List;
import java.util.ServiceLoader;

public class ServiceUtil {

    public static <T> T load(final Class<T> service) {
        final List<ServiceLoader.Provider<T>> providers = ServiceLoader.load(service).stream().toList();
        if (providers.isEmpty()) {
            throw new IllegalStateException("No implementation found for " + service.getName());
        } else if (providers.size() > 1) {
            throw new IllegalStateException("Multiple implementations found for " + service.getName() + ": " + providers.stream().map(p -> p.type().getName()).toList());
        }
        return providers.getFirst().get();
    }

}
