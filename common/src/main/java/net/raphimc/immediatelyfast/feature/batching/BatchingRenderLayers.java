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
package net.raphimc.immediatelyfast.feature.batching;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.function.BiFunction;

import static net.minecraft.util.Util.memoize;

/**
 * Class which defines how different elements (RenderLayer's) are rendered.
 */
public class BatchingRenderLayers {

    public static final BiFunction<Integer, BlendFuncDepthFuncState, RenderLayer> TEXTURE = memoize((id, blendFuncDepthFunc) -> new ImmediatelyFastRenderLayer("texture", VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE, false, () -> {
        blendFuncDepthFunc.saveAndApply();
        RenderSystem.setShaderTexture(0, id);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
    }, blendFuncDepthFunc::revert));

    public static final BiFunction<Integer, BlendFuncDepthFuncState, RenderLayer> COLORED_TEXTURE = memoize((id, blendFuncDepthFunc) -> new ImmediatelyFastRenderLayer("colored_texture", VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR, false, () -> {
        blendFuncDepthFunc.saveAndApply();
        RenderSystem.setShaderTexture(0, id);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
    }, blendFuncDepthFunc::revert));

    private static class ImmediatelyFastRenderLayer extends RenderLayer {

        private ImmediatelyFastRenderLayer(final String name, final VertexFormat.DrawMode drawMode, final VertexFormat vertexFormat, final boolean translucent, final Runnable startAction, final Runnable endAction) {
            super("immediatelyfast_" + name, vertexFormat, drawMode, 2048, false, translucent, startAction, endAction);
        }

    }

}
