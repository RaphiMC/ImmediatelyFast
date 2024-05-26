/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
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
import org.joml.Vector3f;

import java.util.Stack;

public record LightingState(Vector3f shaderLightDirection0, Vector3f shaderLightDirection1) {

    private static final Stack<LightingState> STACK = new Stack<>();

    public static LightingState current() {
        return new LightingState(
                RenderSystem.shaderLightDirections[0],
                RenderSystem.shaderLightDirections[1]
        );
    }

    public void saveAndApply() {
        STACK.push(current());
        this.apply();
    }

    public void revert() {
        STACK.pop().apply();
    }

    public void apply() {
        RenderSystem.setShaderLights(this.shaderLightDirection0, this.shaderLightDirection1);
    }

}
