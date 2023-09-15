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
package net.raphimc.immediatelyfast.injection.mixins.fast_text_lookup;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Function;

@Mixin(value = FontManager.class, priority = 500)
public abstract class MixinFontManager {

    @Shadow
    @Final
    private Map<Identifier, FontStorage> fontStorages;

    @Shadow
    private Map<Identifier, Identifier> idOverrides;

    @Shadow
    protected abstract FontStorage method_27542(Identifier par1);

    @Unique
    private final Map<Identifier, FontStorage> immediatelyFast$overriddenFontStorages = new Object2ObjectOpenHashMap<>();

    @Unique
    private FontStorage immediatelyFast$defaultFontStorage;

    @Unique
    private FontStorage immediatelyFast$unicodeFontStorage;

    @Inject(method = "reload(Lnet/minecraft/client/font/FontManager$ProviderIndex;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("RETURN"))
    private void rebuildOverriddenFontStoragesOnReload(CallbackInfo ci) {
        this.immediatelyFast$rebuildOverriddenFontStorages();
    }

    @Inject(method = "setIdOverrides", at = @At("RETURN"))
    private void rebuildOverriddenFontStoragesOnChange(CallbackInfo ci) {
        this.immediatelyFast$rebuildOverriddenFontStorages();
    }

    @ModifyArg(method = {"createTextRenderer", "createAdvanceValidatingTextRenderer"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;<init>(Ljava/util/function/Function;Z)V"))
    private Function<Identifier, FontStorage> overrideFontStorage(Function<Identifier, FontStorage> original) {
        return id -> {
            // Fast path for default font
            if (MinecraftClient.DEFAULT_FONT_ID.equals(id) && this.immediatelyFast$defaultFontStorage != null) {
                return this.immediatelyFast$defaultFontStorage;
            } else if (MinecraftClient.UNICODE_FONT_ID.equals(id) && this.immediatelyFast$unicodeFontStorage != null) {
                return this.immediatelyFast$unicodeFontStorage;
            }

            // Try to get the font storage from the overridden map otherwise
            final FontStorage storage = this.immediatelyFast$overriddenFontStorages.get(id);
            if (storage != null) {
                return storage;
            }

            // In case some mod is doing cursed stuff call the original function
            return original.apply(id);
        };
    }

    @Unique
    private void immediatelyFast$rebuildOverriddenFontStorages() {
        this.immediatelyFast$overriddenFontStorages.clear();
        this.immediatelyFast$overriddenFontStorages.putAll(this.fontStorages);
        for (Identifier key : this.idOverrides.keySet()) {
            this.immediatelyFast$overriddenFontStorages.put(key, this.method_27542(key));
        }

        this.immediatelyFast$defaultFontStorage = this.immediatelyFast$overriddenFontStorages.get(MinecraftClient.DEFAULT_FONT_ID);
        this.immediatelyFast$unicodeFontStorage = this.immediatelyFast$overriddenFontStorages.get(MinecraftClient.UNICODE_FONT_ID);
    }

}
