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
package net.raphimc.immediatelyfast.injection.mixins.sign_text_buffering;

import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignText;
import net.raphimc.immediatelyfast.injection.interfaces.ISignText;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignText.class)
public abstract class MixinSignText implements ISignText {

    @Shadow
    @Nullable
    private FormattedCharSequence[] renderMessages;

    @Unique
    private boolean immediatelyFast$shouldCache;

    @Unique
    private boolean immediatelyFast$checkedShouldCache;

    @Inject(method = "getRenderMessages", at = @At("RETURN"))
    private void checkShouldCache(final CallbackInfoReturnable<FormattedCharSequence[]> cir) {
        if (!this.immediatelyFast$checkedShouldCache) {
            this.immediatelyFast$checkedShouldCache = true;
            this.immediatelyFast$shouldCache = true;
            for (FormattedCharSequence line : this.renderMessages) {
                if (!this.immediatelyFast$shouldCache) {
                    break;
                }

                line.accept((_, style, _) -> {
                    if (style.isObfuscated()) {
                        this.immediatelyFast$shouldCache = false;
                        return false;
                    }

                    return true;
                });
            }
        }
    }

    @Inject(method = "getRenderMessages", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/SignText;renderMessages:[Lnet/minecraft/util/FormattedCharSequence;", opcode = Opcodes.PUTFIELD))
    private void invalidateCache(final CallbackInfoReturnable<FormattedCharSequence[]> cir) {
        this.immediatelyFast$shouldCache = false;
        this.immediatelyFast$checkedShouldCache = false;
    }

    @Override
    public boolean immediatelyFast$shouldCache() {
        return this.immediatelyFast$shouldCache;
    }

    @Override
    public void immediatelyFast$setShouldCache(final boolean shouldCache) {
        this.immediatelyFast$shouldCache = shouldCache;
    }

}
