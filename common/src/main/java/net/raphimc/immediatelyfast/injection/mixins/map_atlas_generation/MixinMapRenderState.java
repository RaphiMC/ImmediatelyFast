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
package net.raphimc.immediatelyfast.injection.mixins.map_atlas_generation;

import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.resources.Identifier;
import net.raphimc.immediatelyfast.injection.interfaces.IMapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MapRenderState.class)
public abstract class MixinMapRenderState implements IMapRenderState {

    @Unique
    private Identifier immediatelyFast$atlasTextureId;

    @Unique
    private int immediatelyFast$atlasX;

    @Unique
    private int immediatelyFast$atlasY;

    @Override
    public Identifier immediatelyFast$getAtlasTextureId() {
        return this.immediatelyFast$atlasTextureId;
    }

    @Override
    public void immediatelyFast$setAtlasTextureId(final Identifier identifier) {
        this.immediatelyFast$atlasTextureId = identifier;
    }

    @Override
    public int immediatelyFast$getAtlasX() {
        return this.immediatelyFast$atlasX;
    }

    @Override
    public void immediatelyFast$setAtlasX(final int x) {
        this.immediatelyFast$atlasX = x;
    }

    @Override
    public int immediatelyFast$getAtlasY() {
        return this.immediatelyFast$atlasY;
    }

    @Override
    public void immediatelyFast$setAtlasY(final int y) {
        this.immediatelyFast$atlasY = y;
    }
}
