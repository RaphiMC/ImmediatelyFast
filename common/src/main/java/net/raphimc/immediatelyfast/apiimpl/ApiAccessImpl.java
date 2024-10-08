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

import net.raphimc.immediatelyfastapi.ApiAccess;
import net.raphimc.immediatelyfastapi.BatchingAccess;
import net.raphimc.immediatelyfastapi.ConfigAccess;

public class ApiAccessImpl implements ApiAccess {

    @Deprecated
    private final BatchingAccess batchingAccess = new BatchingAccessImpl();
    private final ConfigAccess configAccess = new ConfigAccessImpl();
    private final ConfigAccess runtimeConfigAccess = new RuntimeConfigAccessImpl();

    @Override
    @Deprecated
    public BatchingAccess getBatching() {
        return this.batchingAccess;
    }

    @Override
    public ConfigAccess getConfig() {
        return this.configAccess;
    }

    @Override
    public ConfigAccess getRuntimeConfig() {
        return this.runtimeConfigAccess;
    }

}
