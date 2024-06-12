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
package net.raphimc.immediatelyfast.compat;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.lenni0451.reflect.accessor.FieldAccessor;
import net.lenni0451.reflect.stream.RStream;
import net.raphimc.immediatelyfast.ImmediatelyFast;

import java.util.function.BooleanSupplier;

public class IrisCompat {

    public static boolean IRIS_LOADED = false;

    public static BooleanSupplier isRenderingLevel;
    public static BooleanConsumer renderWithExtendedVertexFormat;
    public static ThreadLocal<Boolean> skipExtension;

    public static void init() {
        IRIS_LOADED = true;
        try {
            final Class<?> immediateStateClass = Class.forName("net.irisshaders.iris.vertices.ImmediateState");

            isRenderingLevel = FieldAccessor.makeGetter(BooleanSupplier.class, null, immediateStateClass.getDeclaredField("isRenderingLevel"));
            renderWithExtendedVertexFormat = FieldAccessor.makeSetter(BooleanConsumer.class, null, immediateStateClass.getDeclaredField("renderWithExtendedVertexFormat"));
            skipExtension = RStream.of(immediateStateClass).fields().by("skipExtension").get();
        } catch (Throwable t) {
            ImmediatelyFast.LOGGER.error("Failed to initialize Iris compatibility. Try updating Iris and ImmediatelyFast before reporting this on GitHub", t);
            System.exit(-1);
        }
    }

}
