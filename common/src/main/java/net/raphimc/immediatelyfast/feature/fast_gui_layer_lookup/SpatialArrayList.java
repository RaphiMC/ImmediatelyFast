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
package net.raphimc.immediatelyfast.feature.fast_gui_layer_lookup;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.ScreenArea;

import java.util.ArrayList;

public class SpatialArrayList<E extends ScreenArea> extends ArrayList<E> {

    private final ScreenRectangleQuadTree tree = new ScreenRectangleQuadTree();
    private int left = Integer.MAX_VALUE;
    private int top = Integer.MAX_VALUE;
    private int right = Integer.MIN_VALUE;
    private int bottom = Integer.MIN_VALUE;
    private int indexedModCount;

    @Override
    public boolean add(final E element) {
        final int previousModCount = this.modCount;
        final boolean result = super.add(element);
        if (this.indexedModCount == previousModCount) {
            final ScreenRectangle bounds = element.bounds();
            if (bounds != null) {
                this.index(bounds);
            }
            this.indexedModCount = this.modCount;
        }
        return result;
    }

    @Override
    public E set(final int index, final E element) {
        final E previous = super.set(index, element);
        this.indexedModCount = this.modCount - 1; // Invalidate the state
        return previous;
    }

    public boolean isStateValid() {
        return this.indexedModCount == this.modCount;
    }

    public boolean intersects(final ScreenRectangle bounds) {
        if (this.left >= bounds.right() || this.right <= bounds.left() || this.top >= bounds.bottom() || this.bottom <= bounds.top()) {
            return false;
        }
        return this.tree.intersects(bounds);
    }

    private void index(final ScreenRectangle bounds) {
        this.left = Math.min(bounds.left(), this.left);
        this.top = Math.min(bounds.top(), this.top);
        this.right = Math.max(bounds.right(), this.right);
        this.bottom = Math.max(bounds.bottom(), this.bottom);
        this.tree.add(bounds);
    }

}
