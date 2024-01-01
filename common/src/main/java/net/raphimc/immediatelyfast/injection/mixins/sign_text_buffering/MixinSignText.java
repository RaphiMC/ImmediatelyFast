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
package net.raphimc.immediatelyfast.injection.mixins.sign_text_buffering;

import net.minecraft.block.entity.SignText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
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
    private Text[] messages;

    @Shadow
    @Final
    private Text[] filteredMessages;

    @Shadow
    @Final
    private DyeColor color;

    @Shadow
    @Final
    private boolean glowing;

    @Shadow
    @Nullable
    private OrderedText[] orderedMessages;

    @Unique
    private boolean immediatelyFast$shouldCache;

    @Unique
    private boolean immediatelyFast$checkedShouldCache;

    @Unique
    private int immediatelyFast$cachedHashCode;

    @Unique
    private boolean immediatelyFast$calculatedHashCode;

    @Inject(method = "getOrderedMessages", at = @At("RETURN"))
    private void checkShouldCache(CallbackInfoReturnable<OrderedText[]> cir) {
        if (!this.immediatelyFast$checkedShouldCache) {
            this.immediatelyFast$checkedShouldCache = true;
            this.immediatelyFast$shouldCache = true;
            for (OrderedText orderedText : this.orderedMessages) {
                if (!this.immediatelyFast$shouldCache) break;

                orderedText.accept((index, style, codePoint) -> {
                    if (style.isObfuscated()) {
                        this.immediatelyFast$shouldCache = false;
                        return false;
                    }

                    return true;
                });
            }
        }
    }

    @Inject(method = "getOrderedMessages", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/SignText;orderedMessages:[Lnet/minecraft/text/OrderedText;", opcode = Opcodes.PUTFIELD))
    private void invalidateCache(CallbackInfoReturnable<OrderedText[]> cir) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MixinSignText that = (MixinSignText) o;
        return glowing == that.glowing && color == that.color && Arrays.equals(messages, that.messages) && Arrays.equals(filteredMessages, that.filteredMessages);
    }

    @Override
    public int hashCode() {
        if (!this.immediatelyFast$calculatedHashCode) {
            this.immediatelyFast$calculatedHashCode = true;
            int result = Objects.hash(color, glowing);
            result = 31 * result + Arrays.hashCode(messages);
            result = 31 * result + Arrays.hashCode(filteredMessages);
            this.immediatelyFast$cachedHashCode = result;
        }

        return this.immediatelyFast$cachedHashCode;
    }

}
