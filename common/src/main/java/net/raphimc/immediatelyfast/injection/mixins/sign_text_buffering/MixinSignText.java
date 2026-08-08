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

import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignText;
import net.raphimc.immediatelyfast.injection.interfaces.ISignText;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Objects;

@Mixin(SignText.class)
public abstract class MixinSignText implements ISignText {

    @Shadow
    @Final
    private Component[] messages;

    @Shadow
    @Final
    private Component[] filteredMessages;

    @Shadow
    @Final
    private DyeColor color;

    @Shadow
    @Final
    private boolean hasGlowingText;

    @Shadow
    @Nullable
    private FormattedCharSequence[] renderMessages;

    @Unique
    private boolean immediatelyFast$shouldCache;

    @Unique
    private boolean immediatelyFast$checkedShouldCache;

    @Unique
    private int immediatelyFast$cachedHashCode;

    @Unique
    private boolean immediatelyFast$calculatedHashCode;

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
        this.immediatelyFast$cachedHashCode = 0;
        this.immediatelyFast$calculatedHashCode = false;
    }

    @Override
    public boolean immediatelyFast$shouldCache() {
        return this.immediatelyFast$shouldCache;
    }

    @Override
    public void immediatelyFast$setShouldCache(final boolean shouldCache) {
        this.immediatelyFast$shouldCache = shouldCache;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MixinSignText that = (MixinSignText) o;
        return hasGlowingText == that.hasGlowingText && color == that.color && Arrays.equals(messages, that.messages) && Arrays.equals(filteredMessages, that.filteredMessages);
    }

    @Override
    public int hashCode() {
        if (!this.immediatelyFast$calculatedHashCode) {
            this.immediatelyFast$calculatedHashCode = true;
            int result = Objects.hash(color, hasGlowingText);
            result = 31 * result + Arrays.hashCode(messages);
            result = 31 * result + Arrays.hashCode(filteredMessages);
            this.immediatelyFast$cachedHashCode = result;
        }

        return this.immediatelyFast$cachedHashCode;
    }

}
