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
package net.raphimc.immediatelyfast.feature.sign_text_buffering;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

public class SignAtlasFramebuffer extends Framebuffer implements AutoCloseable {

    public static final int ATLAS_SIZE = 4096;

    private final Identifier textureId;
    private final Slot rootSlot;

    public SignAtlasFramebuffer(final int id) {
        super("ImmediatelyFast Sign Atlas FBO", false);
        this.resize(ATLAS_SIZE, ATLAS_SIZE);
        this.textureId = Identifier.of("immediatelyfast", "sign_atlas/" + id);
        MinecraftClient.getInstance().getTextureManager().registerTexture(this.textureId, new FboTexture());
        this.rootSlot = new Slot(null, 0, 0, ATLAS_SIZE, ATLAS_SIZE);
    }

    public int bind(final boolean setViewport) {
        final int previousFramebuffer = GL11C.glGetInteger(GL30C.GL_FRAMEBUFFER_BINDING);
        final int fbo = ((GlTexture) SignAtlasFramebuffer.this.colorAttachment).getOrCreateFramebuffer(((GlBackend) RenderSystem.getDevice()).getBufferManager(), null);
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, fbo);
        if (setViewport) {
            GL11C.glViewport(0, 0, ATLAS_SIZE, ATLAS_SIZE);
        }
        return previousFramebuffer;
    }

    public void unbind(final int previousFbo) {
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, previousFbo);
        GL11C.glViewport(0, 0, MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight());
    }

    public Slot findSlot(final int width, final int height) {
        return this.rootSlot.findSlot(width, height);
    }

    public void clear() {
        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.getColorAttachment(), 0);

        this.rootSlot.subSlot1 = null;
        this.rootSlot.subSlot2 = null;
    }

    public Identifier getTextureId() {
        return this.textureId;
    }

    @Override
    public void close() {
        this.delete();
    }

    public class Slot {

        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final Slot parentSlot;
        public Slot subSlot1;
        public Slot subSlot2;
        public boolean occupied;

        public Slot(final Slot parentSlot, final int x, final int y, final int width, final int height) {
            this.parentSlot = parentSlot;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void markFree() {
            if (this.subSlot1 != null || this.subSlot2 != null) {
                throw new UnsupportedOperationException("Cannot mark slot as free if it has sub slots");
            }
            if (!this.occupied) {
                throw new UnsupportedOperationException("Cannot mark slot as free if it is not occupied");
            }
            this.occupied = false;
            removeUnoccupiedSubSlots(this);

            GlStateManager._scissorBox(this.x, ATLAS_SIZE - this.y - this.height, this.width, this.height);
            GlStateManager._enableScissorTest();
            final int previousFbo = SignAtlasFramebuffer.this.bind(false);
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT);
            SignAtlasFramebuffer.this.unbind(previousFbo);
            GlStateManager._disableScissorTest();
        }

        public Slot findSlot(final int width, final int height) {
            if (this.subSlot1 != null && this.subSlot2 != null) {
                Slot slot = this.subSlot1.findSlot(width, height);
                if (slot == null) {
                    slot = this.subSlot2.findSlot(width, height);
                }

                return slot;
            } else if (this.occupied) {
                return null;
            } else {
                if (width > this.width || height > this.height) {
                    return null;
                } else if (width == this.width && height == this.height) {
                    this.occupied = true;
                    return this;
                } else {
                    final int k = this.width - width;
                    final int l = this.height - height;
                    if (k > l) {
                        this.subSlot1 = new Slot(this, this.x, this.y, width, this.height);
                        this.subSlot2 = new Slot(this, this.x + width, this.y, this.width - width, this.height);
                    } else {
                        this.subSlot1 = new Slot(this, this.x, this.y, this.width, height);
                        this.subSlot2 = new Slot(this, this.x, this.y + height, this.width, this.height - height);
                    }

                    return this.subSlot1.findSlot(width, height);
                }
            }
        }

        private static void removeUnoccupiedSubSlots(final Slot slot) {
            if (slot == null) return;
            removeUnoccupiedSubSlots(slot.parentSlot);
            final boolean subSlot1Unoccupied = slot.subSlot1 != null && !hasOccupiedSlot(slot.subSlot1);
            final boolean subSlot2Unoccupied = slot.subSlot2 != null && !hasOccupiedSlot(slot.subSlot2);

            if (subSlot1Unoccupied && subSlot2Unoccupied) {
                slot.subSlot1 = null;
                slot.subSlot2 = null;
            }
        }

        private static boolean hasOccupiedSlot(final Slot slot) {
            if (slot == null) return false;
            if (slot.occupied) return true;
            return hasOccupiedSlot(slot.subSlot1) || hasOccupiedSlot(slot.subSlot2);
        }

    }

    private class FboTexture extends AbstractTexture {

        private FboTexture() {
            this.glTexture = SignAtlasFramebuffer.this.colorAttachment;
            this.glTextureView = RenderSystem.getDevice().createTextureView(this.glTexture);
        }

        @Override
        public void close() {
        }

    }

}
