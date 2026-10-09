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
package net.raphimc.immediatelyfast.feature.debug_chart_batching;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.ColoredRectangleRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DebugChartBatch {

    private static final ThreadLocal<DebugChartBatch> ACTIVE = new ThreadLocal<>();

    private final DebugChartBatch parent;

    private GuiRenderState owner;
    private final List<ColoredRectangleRenderState> rectangles = new ArrayList<>();
    private boolean flushing;

    private DebugChartBatch(final DebugChartBatch parent) {
        this.parent = parent;
    }

    public static DebugChartBatch begin() {
        final DebugChartBatch parent = ACTIVE.get();
        if (parent != null) {
            parent.flush();
        }

        final DebugChartBatch batch = new DebugChartBatch(parent);
        ACTIVE.set(batch);
        return batch;
    }

    public void end() {
        try {
            this.flush();
        } finally {
            if (this.parent == null) {
                ACTIVE.remove();
            } else {
                ACTIVE.set(this.parent);
            }
        }
    }

    public static void barrier() {
        final DebugChartBatch batch = ACTIVE.get();
        if (batch != null) {
            batch.flush();
        }
    }

    public static boolean capture(final GuiRenderState owner, final GuiElementRenderState element) {
        final DebugChartBatch batch = ACTIVE.get();
        if (batch == null || batch.flushing) {
            return false;
        }

        if (!(element instanceof ColoredRectangleRenderState rectangle) || rectangle.bounds() == null) {
            batch.flush();
            return false;
        }

        if (!batch.rectangles.isEmpty()) {
            final ColoredRectangleRenderState first = batch.rectangles.get(0);

            if (batch.owner != owner
                    || first.pipeline() != rectangle.pipeline()
                    || !Objects.equals(first.textureSetup(), rectangle.textureSetup())
                    || !Objects.equals(first.scissorArea(), rectangle.scissorArea())) {
                batch.flush();
            }
        }

        batch.owner = owner;
        batch.rectangles.add(rectangle);
        return true;
    }

    private void flush() {
        if (this.flushing || this.rectangles.isEmpty()) {
            return;
        }

        this.flushing = true;
        try {
            final GuiElementRenderState element = this.rectangles.size() == 1 ? this.rectangles.get(0) : new Rectangles(this.rectangles);
            this.owner.addGuiElement(element);
        } finally {
            this.rectangles.clear();
            this.owner = null;
            this.flushing = false;
        }
    }

    private static final class Rectangles implements GuiElementRenderState {

        private final List<ColoredRectangleRenderState> rectangles;
        private final ScreenRectangle bounds;

        private Rectangles(final List<ColoredRectangleRenderState> rectangles) {
            this.rectangles = List.copyOf(rectangles);

            int left = Integer.MAX_VALUE;
            int top = Integer.MAX_VALUE;
            int right = Integer.MIN_VALUE;
            int bottom = Integer.MIN_VALUE;

            for (final ColoredRectangleRenderState rectangle : this.rectangles) {
                final ScreenRectangle bounds = rectangle.bounds();
                left = Math.min(left, bounds.left());
                top = Math.min(top, bounds.top());
                right = Math.max(right, bounds.right());
                bottom = Math.max(bottom, bounds.bottom());
            }

            this.bounds = new ScreenRectangle(left, top, right - left, bottom - top);
        }

        @Override
        public void buildVertices(final VertexConsumer consumer) {
            for (final ColoredRectangleRenderState rectangle : this.rectangles) {
                rectangle.buildVertices(consumer);
            }
        }

        @Override
        public RenderPipeline pipeline() {
            return this.rectangles.get(0).pipeline();
        }

        @Override
        public TextureSetup textureSetup() {
            return this.rectangles.get(0).textureSetup();
        }

        @Override
        public ScreenRectangle scissorArea() {
            return this.rectangles.get(0).scissorArea();
        }

        @Override
        public ScreenRectangle bounds() {
            return this.bounds;
        }

    }

}
