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

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import org.lwjgl.opengl.GL15C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteBuffer;

@Mixin(value = VertexBuffer.class, priority = 500)
public abstract class MixinVertexBuffer {

    @Unique
    private int vertexBufferSize;

    @Unique
    private int indexBufferSize;

    @Redirect(method = "uploadInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;" +
            "glBufferData(ILjava/nio/ByteBuffer;I)V"))
    private void optimizeUploading(int target, ByteBuffer data, int usage) {
        if (data.remaining() > this.indexBufferSize) {
            this.indexBufferSize = data.remaining();
            RenderSystem.glBufferData(target, data, GL15C.GL_DYNAMIC_DRAW);
        } else if (data.remaining() > this.vertexBufferSize) {
            this.vertexBufferSize = data.remaining();
            RenderSystem.glBufferData(target, data, GL15C.GL_DYNAMIC_DRAW);
        } else {
            GL15C.glBufferSubData(target, 0, data);
        }
    }
}
