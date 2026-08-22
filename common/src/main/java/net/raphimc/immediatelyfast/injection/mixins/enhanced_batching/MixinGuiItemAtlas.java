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
package net.raphimc.immediatelyfast.injection.mixins.enhanced_batching;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.gui.render.DynamicAtlasAllocator;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.raphimc.immediatelyfast.injection.interfaces.IGuiItemAtlas;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiItemAtlas.class)
public abstract class MixinGuiItemAtlas implements IGuiItemAtlas {

    @Shadow
    @Final
    private int textureSize;

    @Shadow
    @Final
    private int slotTextureSize;

    @Shadow
    @Final
    private GpuTexture texture;

    @Shadow
    @Final
    private GpuTexture depthTexture;

    @Shadow
    @Final
    @Mutable
    private DynamicAtlasAllocator<Object> allocator;

    @Override
    public void immediatelyFast$reset() {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(this.texture, GuiRenderer.CLEAR_COLOR, this.depthTexture, 0D);
        final int slotsPerSide = this.textureSize / this.slotTextureSize;
        this.allocator = new DynamicAtlasAllocator<>(slotsPerSide, slotsPerSide);
    }

}
