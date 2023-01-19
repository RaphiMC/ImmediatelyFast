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
package net.raphimc.immediatelyfast.injection;

import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ImmediatelyFastMixinPlugin implements IMixinConfigPlugin {

    private String mixinPackage;

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage + ".";

        ImmediatelyFast.loadConfig();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(this.mixinPackage)) return false;

        final String mixinName = mixinClassName.substring(this.mixinPackage.length());
        final String packageName = mixinName.substring(0, mixinName.lastIndexOf('.'));

        if (!ImmediatelyFast.config.font_atlas_resizing && packageName.startsWith("font_atlas_resizing")) {
            return false;
        }
        if (!ImmediatelyFast.config.map_atlas_generation && packageName.startsWith("map_atlas_generation")) {
            return false;
        }
        if (!ImmediatelyFast.config.hud_batching && packageName.startsWith("hud_batching")) {
            return false;
        }
        if (!ImmediatelyFast.config.fast_text_lookup && packageName.startsWith("fast_text_lookup")) {
            return false;
        }
        if (!ImmediatelyFast.config.fast_buffer_upload && packageName.startsWith("fast_buffer_upload")) {
            return false;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

}
