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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.block.entity.SignText;
import org.joml.Matrix4f;

import java.util.concurrent.TimeUnit;

public class SignTextCache implements ResourceManagerReloadListener {

    public final SignAtlasRenderTarget signAtlasRenderTarget;
    public final RenderType renderType;
    public final GpuBufferSlice signProjectionMatrix;
    public final Cache<SignText, SignAtlasRenderTarget.Slot> slotCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .removalListener(notification -> {
                if (notification.getCause().equals(RemovalCause.EXPLICIT)) return;

                final SignAtlasRenderTarget.Slot slot = (SignAtlasRenderTarget.Slot) notification.getValue();
                if (slot != null) {
                    slot.markFree();
                }
            })
            .build();
    public boolean lockFramebuffer = false;
    public boolean lockViewport = false;

    public SignTextCache() {
        RenderSystem.assertOnRenderThread();
        this.signAtlasRenderTarget = new SignAtlasRenderTarget(0);
        this.renderType = RenderTypes.text(this.signAtlasRenderTarget.getTextureId());
        final ProjectionMatrixBuffer projectionMatrixBuffer = new ProjectionMatrixBuffer("immediatelyfast:sign_atlas_text");
        this.signProjectionMatrix = projectionMatrixBuffer.getBuffer(new Matrix4f().setOrtho(0F, SignAtlasRenderTarget.ATLAS_SIZE, SignAtlasRenderTarget.ATLAS_SIZE, 0F, -1000F, 1000F));
    }

    public void clearCache() {
        RenderSystem.assertOnRenderThread();
        this.slotCache.invalidateAll();
        this.signAtlasRenderTarget.clear();
    }

    @Override
    public void onResourceManagerReload(final ResourceManager manager) {
        this.clearCache();
    }

}
