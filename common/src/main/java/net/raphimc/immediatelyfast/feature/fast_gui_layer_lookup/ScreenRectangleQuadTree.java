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

import java.util.Arrays;

public class ScreenRectangleQuadTree {

    private static final int CHILD_COUNT = 4;

    private Node root;

    public void add(final ScreenRectangle bounds) {
        if (this.root == null) {
            this.createRoot(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        } else {
            this.ensureRootContains(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        }
        this.root.add(bounds, 0);
    }

    public boolean intersects(final ScreenRectangle bounds) {
        return this.root != null && this.root.intersects(bounds);
    }

    private void createRoot(final long left, final long top, final long right, final long bottom) {
        final long extent = Math.max(1L, Math.max(right - left, bottom - top));
        final long size = 1L << (Long.SIZE - Long.numberOfLeadingZeros(extent - 1)); // Round up to a power of two
        this.root = new Node(left, top, size);
    }

    private void ensureRootContains(final long left, final long top, final long right, final long bottom) {
        while (left < this.root.x || top < this.root.y || right > this.root.x + this.root.size || bottom > this.root.y + this.root.size) {
            final Node previous = this.root;
            final boolean growLeft = left < previous.x;
            final boolean growUp = top < previous.y;
            if (previous.children == null) { // A leaf can grow in place
                if (growLeft) {
                    previous.x -= previous.size;
                }
                if (growUp) {
                    previous.y -= previous.size;
                }
                previous.size *= 2;
            } else { // Preserve existing child regions by wrapping the root
                this.root = new Node(growLeft ? previous.x - previous.size : previous.x, growUp ? previous.y - previous.size : previous.y, previous.size * 2);
                this.root.children = new Node[CHILD_COUNT];
                this.root.children[(growLeft ? 1 : 0) | (growUp ? 2 : 0)] = previous;
            }
        }
    }

    private static final class Node {

        private static final int LEAF_CAPACITY = 12;
        private static final int MAX_DEPTH = 12;

        private long x;
        private long y;
        private long size;
        private ScreenRectangle[] entries;
        private int entryCount;
        private Node[] children;

        private Node(final long x, final long y, final long size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }

        private void add(final ScreenRectangle bounds, int depth) {
            Node node = this;
            while (node.children != null || node.shouldSplit(depth)) {
                if (node.children == null) {
                    node.split(depth);
                }
                final int quadrant = node.getContainingQuadrant(bounds);
                if (quadrant < 0) {
                    break;
                }
                node = node.getOrCreateChild(quadrant);
                depth++;
            }
            node.addEntry(bounds);
        }

        private boolean shouldSplit(final int depth) {
            return this.children == null && this.entryCount >= LEAF_CAPACITY && depth < MAX_DEPTH && this.size > 1;
        }

        private void split(final int depth) {
            this.children = new Node[CHILD_COUNT];
            for (int i = this.entryCount - 1; i >= 0; i--) {
                final ScreenRectangle entry = this.entries[i];
                final int quadrant = this.getContainingQuadrant(entry);
                if (quadrant >= 0) {
                    this.getOrCreateChild(quadrant).add(entry, depth + 1);
                    System.arraycopy(this.entries, i + 1, this.entries, i, this.entryCount - i - 1);
                    this.entries[--this.entryCount] = null;
                }
            }
        }

        private void addEntry(final ScreenRectangle bounds) {
            if (this.entries == null) {
                this.entries = new ScreenRectangle[LEAF_CAPACITY];
            } else if (this.entryCount == this.entries.length) {
                this.entries = Arrays.copyOf(this.entries, this.entryCount * 2);
            }
            this.entries[this.entryCount++] = bounds;
        }

        private boolean intersects(final ScreenRectangle bounds) {
            if (bounds.left() >= this.x + this.size || bounds.right() <= this.x || bounds.top() >= this.y + this.size || bounds.bottom() <= this.y) {
                return false; // Skip the entire subtree
            }
            for (int i = 0; i < this.entryCount; i++) {
                if (this.entries[i].intersects(bounds)) {
                    return true;
                }
            }
            if (this.children != null) {
                for (Node child : this.children) {
                    if (child != null && child.intersects(bounds)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private Node getOrCreateChild(final int quadrant) {
            if (this.children[quadrant] == null) {
                final long half = this.size / 2;
                this.children[quadrant] = new Node(this.x + ((quadrant & 1) != 0 ? half : 0), this.y + ((quadrant & 2) != 0 ? half : 0), half);
            }
            return this.children[quadrant];
        }

        private int getContainingQuadrant(final ScreenRectangle bounds) {
            final long midX = this.x + this.size / 2;
            final long midY = this.y + this.size / 2;
            final int column = bounds.right() <= midX ? 0 : bounds.left() >= midX ? 1 : -1;
            final int row = bounds.bottom() <= midY ? 0 : bounds.top() >= midY ? 2 : -1;
            return column < 0 || row < 0 ? -1 : column | row;
        }

    }

}
