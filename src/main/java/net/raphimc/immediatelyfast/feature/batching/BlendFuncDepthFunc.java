/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.immediatelyfast.feature.batching;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;

public record BlendFuncDepthFunc(int GL_BLEND_SRC_RGB, int GL_BLEND_SRC_ALPHA, int GL_BLEND_DST_RGB, int GL_BLEND_DST_ALPHA, int GL_DEPTH_FUNC) {

    public static BlendFuncDepthFunc current() {
        return new BlendFuncDepthFunc(
                GL11C.glGetInteger(GL14C.GL_BLEND_SRC_RGB),
                GL11C.glGetInteger(GL14C.GL_BLEND_SRC_ALPHA),
                GL11C.glGetInteger(GL14C.GL_BLEND_DST_RGB),
                GL11C.glGetInteger(GL14C.GL_BLEND_DST_ALPHA),
                GL11C.glGetInteger(GL11C.GL_DEPTH_FUNC)
        );
    }

    public void apply() {
        RenderSystem.blendFuncSeparate(GL_BLEND_SRC_RGB, GL_BLEND_DST_RGB, GL_BLEND_SRC_ALPHA, GL_BLEND_DST_ALPHA);
        RenderSystem.depthFunc(GL_DEPTH_FUNC);
    }

}
