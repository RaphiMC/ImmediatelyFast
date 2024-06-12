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
package net.raphimc.immediatelyfast.injection.mixins.core;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.raphimc.immediatelyfast.ImmediatelyFast;
import net.raphimc.immediatelyfast.feature.core.BatchableBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.SequencedMap;

@Mixin(VertexConsumerProvider.class)
public interface MixinVertexConsumerProvider {

    /**
     * @author RK_01
     * @reason Universal Batching
     */
    @Overwrite
    static VertexConsumerProvider.Immediate immediate(SequencedMap<RenderLayer, BufferAllocator> layerBuffers, BufferAllocator fallbackBuffer) {
        if (ImmediatelyFast.config.debug_only_and_not_recommended_disable_universal_batching) {
            return new VertexConsumerProvider.Immediate(fallbackBuffer, layerBuffers);
        }

        // Don't free the fallback buffer. Who knows what else it might get used for outside of this method (https://github.com/RaphiMC/ImmediatelyFast/issues/101)
        // Pass the fallback buffer because some mods access it directly (https://github.com/Team-EnderIO/EnderIO/blob/a67e6dc0dfebf67cd13075ac6aadb9d4598072e8/src/machines/java/com/enderio/machines/client/gui/widget/ioconfig/IOConfigWidget.java#L403)
        return new BatchableBufferSource(fallbackBuffer, layerBuffers);
    }

}
