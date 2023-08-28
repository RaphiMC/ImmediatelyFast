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

import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextRenderer.Drawer.class)
public abstract class MixinTextRenderer_Drawer {

    @Shadow
    @Final
    VertexConsumerProvider vertexConsumers;

    /**
     * Fixes <a href="https://github.com/RaphiMC/ImmediatelyFast/issues/81">https://github.com/RaphiMC/ImmediatelyFast/issues/81</a>
     */
    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/Glyph;getAdvance(Z)F"))
    private float fixNegativeAdvanceGlyphs(Glyph instance, boolean bold) {
        final float advance = instance.getAdvance(bold);
        if (advance < 0 && !ImmediatelyFast.config.experimental_disable_resource_pack_conflict_handling) {
            if (this.vertexConsumers instanceof VertexConsumerProvider.Immediate) {
                ((VertexConsumerProvider.Immediate) this.vertexConsumers).drawCurrentLayer();
            }
        }
        return advance;
    }

}
