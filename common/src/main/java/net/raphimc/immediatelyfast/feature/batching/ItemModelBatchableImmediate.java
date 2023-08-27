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
package net.raphimc.immediatelyfast.feature.batching;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.PlayerScreenHandler;
import net.raphimc.immediatelyfast.feature.core.BatchableImmediate;

public class ItemModelBatchableImmediate extends BatchableImmediate {

    private final boolean guiDepthLighting;

    public ItemModelBatchableImmediate(final boolean guiDepthLighting) {
        super(BatchingBuffers.createLayerBuffers(
                RenderLayer.getArmorGlint(),
                RenderLayer.getArmorEntityGlint(),
                RenderLayer.getGlint(),
                RenderLayer.getDirectGlint(),
                RenderLayer.getGlintTranslucent(),
                RenderLayer.getEntityGlint(),
                RenderLayer.getDirectEntityGlint()
        ));

        this.guiDepthLighting = guiDepthLighting;
    }

    @Override
    public void draw() {
        RenderSystem.getModelViewStack().push();
        RenderSystem.getModelViewStack().loadIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableBlend();
        if (this.guiDepthLighting) {
            DiffuseLighting.enableGuiDepthLighting();
        } else {
            DiffuseLighting.disableGuiDepthLighting();
        }
        super.draw();
        if (!this.guiDepthLighting) {
            DiffuseLighting.enableGuiDepthLighting();
        }
        RenderSystem.disableBlend();
        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();

    }

}
