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
package net.raphimc.immediatelyfast.injection.mixins.fast_text_lookup;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.renderer.feature.NameTagFeatureRenderer$GlyphRenderer")
public abstract class MixinNameTagFeatureRenderer_GlyphRenderer {

    @Unique
    private RenderType immediatelyFast$lastRenderType;

    @Unique
    private VertexConsumer immediatelyFast$lastVertexConsumer;

    @Redirect(method = "acceptRenderable", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/NameTagFeatureRenderer;getVertexBuilder(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer reduceGetVertexBuilderCalls(final NameTagFeatureRenderer instance, final RenderType renderType) {
        if (this.immediatelyFast$lastRenderType != renderType) {
            this.immediatelyFast$lastRenderType = renderType;
            this.immediatelyFast$lastVertexConsumer = instance.getVertexBuilder(renderType);
        }
        return this.immediatelyFast$lastVertexConsumer;
    }

}
