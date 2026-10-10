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
package net.raphimc.immediatelyfast.injection.mixins.debug_chart_batching;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debugchart.AbstractDebugChart;
import net.raphimc.immediatelyfast.feature.debug_chart_batching.DebugChartBatch;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractDebugChart.class)
public abstract class MixinAbstractDebugChart {

    @WrapMethod(method = "extractRenderState")
    private void batchChartRectangles(final GuiGraphicsExtractor graphics, final int x, final int width, final int bottom, final Operation<Void> original) {
        final DebugChartBatch batch = DebugChartBatch.begin();
        try {
            original.call(graphics, x, width, bottom);
        } finally {
            batch.end();
        }
    }

}
