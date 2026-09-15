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
package net.raphimc.immediatelyfast.injection.mixins.enhanced_batching.compat.iris;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.raphimc.immediatelyfast.injection.interfaces.IIris_OuterWrappedRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.layer.OuterWrappedRenderType")
public abstract class MixinIris_OuterWrappedRenderType implements IIris_OuterWrappedRenderType {

    @Shadow
    @Final
    private RenderType wrapped;

    @Override
    public RenderType immediatelyFast$getWrapped() {
        return this.wrapped;
    }

}
