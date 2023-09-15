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
package net.raphimc.immediatelyfast.injection.mixins.fast_text_lookup;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextRenderer.Drawer.class)
public abstract class MixinTextRenderer_Drawer {

    @Unique
    private RenderLayer immediatelyFast$lastRenderLayer;

    @Unique
    private VertexConsumer immediatelyFast$lastVertexConsumer;

    @Unique
    private Identifier immediatelyFast$lastFont;

    @Unique
    private FontStorage immediatelyFast$lastFontStorage;

    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"))
    private VertexConsumer reduceGetBufferCalls(VertexConsumerProvider instance, RenderLayer renderLayer) {
        // The buffer got drawn while rendering the text, so we need to reset the cached data
        final boolean isBufferInvalid = this.immediatelyFast$lastVertexConsumer instanceof BufferBuilder bufferBuilder && !bufferBuilder.isBuilding();

        if (!isBufferInvalid && this.immediatelyFast$lastRenderLayer == renderLayer) {
            return this.immediatelyFast$lastVertexConsumer;
        }

        this.immediatelyFast$lastRenderLayer = renderLayer;
        return this.immediatelyFast$lastVertexConsumer = instance.getBuffer(renderLayer);
    }

    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getFontStorage(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/font/FontStorage;"))
    private FontStorage reduceGetFontStorageCalls(TextRenderer instance, Identifier id) {
        if (this.immediatelyFast$lastFont == id) {
            return this.immediatelyFast$lastFontStorage;
        }

        this.immediatelyFast$lastFont = id;
        return this.immediatelyFast$lastFontStorage = instance.getFontStorage(id);
    }

}
