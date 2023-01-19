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

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.raphimc.immediatelyfast.injection.interfaces.IVertexFormat;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL15C;
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
    @Shadow private VertexFormat vertexFormat;
    @Shadow public abstract VertexFormat getVertexFormat();
    @Shadow private int vertexBufferId;
    @Shadow private int indexBufferId;
    @Shadow private VertexFormat.DrawMode drawMode;
    @Shadow private int vertexCount;
    @Unique private int indexBufferSize;


    @Inject(method = "uploadInternal", at = @At(target = "Lnet/minecraft/client/render" +
            "/BufferBuilder$DrawArrayParameters;getVertexCount()I", value = "INVOKE"))
    private void replace_value_format(BufferBuilder buffer, CallbackInfo ci) {
        vertexFormat = configureVertexFormat(buffer.popData().getFirst(), buffer.popData().getSecond());
    }


    private VertexFormat configureVertexFormat(BufferBuilder.DrawArrayParameters parameters, ByteBuffer data) {
        boolean bl = false;
        if (!parameters.getVertexFormat().equals(this.getVertexFormat())) {
            if (this.getVertexFormat() != null) {
                ((IVertexFormat)vertexFormat).clearState();
            }

            GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, this.vertexBufferId);
            ((IVertexFormat)parameters.getVertexFormat()).setUpState();
            bl = true;
        }

        if (parameters.hasNoIndexBuffer()) {
            if (!bl) {
                GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, this.vertexBufferId);
            }

            if (data.remaining() > this.indexBufferSize) {
                this.indexBufferSize = data.remaining();
                RenderSystem.glBufferData(GlConst.GL_ARRAY_BUFFER, data, GL15C.GL_DYNAMIC_DRAW);
            } else {
                GL15C.glBufferSubData(GlConst.GL_ARRAY_BUFFER, 0, data);
            }
        }

        return parameters.getVertexFormat();
    }

    /*@Nullable
    private RenderSystem.IndexBuffer configureIndexBuffer(BufferBuilder.DrawArrayParameters parameters, ByteBuffer data) {
        if (!parameters.hasNoIndexBuffer()) {
            GlStateManager._glBindBuffer(GlConst.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
            RenderSystem.glBufferData(GlConst.GL_ELEMENT_ARRAY_BUFFER, data, GlConst.GL_STATIC_DRAW);
            return null;
        } else {
            RenderSystem.IndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(parameters.getMode(),
                    parameters.getCount());
            if (indexBuffer != RenderSystem.getSequentialBuffer(this.drawMode, this.vertexCount) || !indexBuffer.isSizeLessThanOrEqual(parameters.getCount())) {
                indexBuffer.bindAndGrow(parameters.getCount());
            }

            return indexBuffer;
        }
    }*/
}
