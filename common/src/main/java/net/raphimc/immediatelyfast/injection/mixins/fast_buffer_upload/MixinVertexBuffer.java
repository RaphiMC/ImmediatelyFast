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
package net.raphimc.immediatelyfast.injection.mixins.fast_buffer_upload;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.fast_buffer_upload.PersistentMappedStreamingBuffer;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL45C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(value = VertexBuffer.class, priority = 500)
public abstract class MixinVertexBuffer {

    @Shadow
    private int vertexBufferId;

    @Shadow
    private int indexBufferId;

    @Unique
    private int vertexBufferSize = -1;

    @Unique
    private int indexBufferSize = -1;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glGenBuffers()I"))
    private int allocateOptimizableBuffer() {
        if (ImmediatelyFast.persistentMappedStreamingBuffer != null) {
            return GL45C.glCreateBuffers();
        } else {
            return GL15C.glGenBuffers();
        }
    }

    @Redirect(method = "configureVertexFormat", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;glBufferData(ILjava/nio/ByteBuffer;I)V"))
    private void optimizeVertexDataUploading(int target, ByteBuffer data, int usage, @Local BufferBuilder.DrawArrayParameters parameters) {
        final int dataSize = data.remaining();
        if (dataSize == 0 && this.vertexBufferSize != -1) return;

        final PersistentMappedStreamingBuffer streamingBuffer = ImmediatelyFast.persistentMappedStreamingBuffer;
        if (dataSize <= this.vertexBufferSize) {
            if (streamingBuffer != null && dataSize <= streamingBuffer.getSize()) {
                streamingBuffer.addUpload(this.vertexBufferId, data);
                return;
            } else if (streamingBuffer == null && ImmediatelyFast.runtimeConfig.legacy_fast_buffer_upload) {
                GL15C.glBufferSubData(target, 0, data);
                return;
            }
        }

        this.vertexBufferSize = dataSize;
        if (streamingBuffer != null) {
            GL15C.glDeleteBuffers(this.vertexBufferId);
            this.vertexBufferId = GL45C.glCreateBuffers();
            GL45C.glNamedBufferStorage(this.vertexBufferId, data, 0);
            GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBufferId);
            parameters.format().setupState();
        } else {
            GL15C.glBufferData(target, data, usage);
        }
    }

    @Redirect(method = "configureIndexBuffer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;glBufferData(ILjava/nio/ByteBuffer;I)V"))
    private void optimizeIndexDataUploading(int target, ByteBuffer data, int usage) {
        final int dataSize = data.remaining();
        if (dataSize == 0 && this.indexBufferSize != -1) return;

        final PersistentMappedStreamingBuffer streamingBuffer = ImmediatelyFast.persistentMappedStreamingBuffer;
        if (dataSize <= this.indexBufferSize) {
            if (streamingBuffer != null && dataSize <= streamingBuffer.getSize()) {
                streamingBuffer.addUpload(this.indexBufferId, data);
                return;
            } else if (streamingBuffer == null && ImmediatelyFast.runtimeConfig.legacy_fast_buffer_upload) {
                GL15C.glBufferSubData(target, 0, data);
                return;
            }
        }

        this.indexBufferSize = dataSize;
        if (streamingBuffer != null) {
            GL15C.glDeleteBuffers(this.indexBufferId);
            this.indexBufferId = GL45C.glCreateBuffers();
            GL45C.glNamedBufferStorage(this.indexBufferId, data, 0);
            GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
        } else {
            GL15C.glBufferData(target, data, usage);
        }
    }

    @Inject(method = "upload", at = @At("RETURN"))
    private void flushBuffers(CallbackInfo ci) {
        if (ImmediatelyFast.persistentMappedStreamingBuffer != null) {
            ImmediatelyFast.persistentMappedStreamingBuffer.flush();
        }
    }

}
