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
package net.raphimc.immediatelyfast.injection.mixins.core.compat;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = TextRenderer.class, priority = 1500)
public abstract class MixinTextRenderer {

    @ModifyArg(method = {"draw(Ljava/lang/String;FFILorg/joml/Matrix4f;ZZ)I", "draw(Lnet/minecraft/text/OrderedText;FFILorg/joml/Matrix4f;Z)I"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;immediate(Lnet/minecraft/client/render/BufferBuilder;)Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;"))
    private BufferBuilder handleTextUniversalBatching(BufferBuilder buffer) {
        return ImmediatelyFast.runtimeConfig.universal_batching_text ? buffer : null;
    }

}
