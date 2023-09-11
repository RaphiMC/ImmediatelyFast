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
    private boolean shouldCache;

    @Unique
    private boolean checkedShouldCache;

    @Unique
    private int cachedHashCode;

    @Unique
    private boolean calculatedHashCode;

    @Inject(method = "getOrderedMessages", at = @At("RETURN"))
    private void checkShouldCache(CallbackInfoReturnable<OrderedText[]> cir) {
        if (!this.checkedShouldCache) {
            this.checkedShouldCache = true;
            this.shouldCache = true;
            for (OrderedText orderedText : this.orderedMessages) {
                if (!this.shouldCache) break;

                orderedText.accept((index, style, codePoint) -> {
                    if (style.isObfuscated()) {
                        this.shouldCache = false;
                        return false;
                    }

                    return true;
                });
            }
        }
    }

    @Inject(method = "getOrderedMessages", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/SignText;orderedMessages:[Lnet/minecraft/text/OrderedText;", opcode = Opcodes.PUTFIELD))
    private void invalidateCache(CallbackInfoReturnable<OrderedText[]> cir) {
        this.shouldCache = false;
        this.checkedShouldCache = false;
        this.cachedHashCode = 0;
        this.calculatedHashCode = false;
    }

    @Override
    public boolean shouldCache() {
        return this.shouldCache;
    }

    @Override
    public void setShouldCache(final boolean shouldCache) {
        this.shouldCache = shouldCache;
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
        if (!this.calculatedHashCode) {
            this.calculatedHashCode = true;
            int result = Objects.hash(color, glowing);
            result = 31 * result + Arrays.hashCode(messages);
            result = 31 * result + Arrays.hashCode(filteredMessages);
            this.cachedHashCode = result;
        }

        return this.cachedHashCode;
    }

}
