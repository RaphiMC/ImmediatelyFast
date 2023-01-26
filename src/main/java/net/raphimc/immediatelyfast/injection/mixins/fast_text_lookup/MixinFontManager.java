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
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mixin(value = FontManager.class, priority = 500)
public abstract class MixinFontManager {

    @Mutable
    @Shadow
    @Final
    private ResourceReloader resourceReloadListener;

    @Shadow
    @Final
    Map<Identifier, FontStorage> fontStorages;

    @Shadow
    private Map<Identifier, Identifier> idOverrides;

    @Shadow
    protected abstract FontStorage method_27542(Identifier par1);

    @Unique
    private final Map<Identifier, FontStorage> overriddenFontStorages = new Object2ObjectOpenHashMap<>();

    @Unique
    private FontStorage defaultFontStorage;

    @Unique
    private FontStorage unicodeFontStorage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void if$hookReloader(TextureManager manager, CallbackInfo ci) {
        this.resourceReloadListener = new SinglePreparationResourceReloader<Map<Identifier, List<Font>>>() {
            private final ResourceReloader delegate = resourceReloadListener;

            @Override
            protected Map<Identifier, List<Font>> prepare(ResourceManager manager, Profiler profiler) {
                return ((ISinglePreparationResourceReloader) this.delegate).invokePrepare(manager, profiler);
            }

            @Override
            protected void apply(Map<Identifier, List<Font>> prepared, ResourceManager manager, Profiler profiler) {
                ((ISinglePreparationResourceReloader) this.delegate).invokeApply(prepared, manager, profiler);
                if$rebuildOverriddenFontStorages();
            }

            @Override
            public String getName() {
                return this.delegate.getName();
            }
        };
    }

    @Inject(method = "setIdOverrides", at = @At("RETURN"))
    private void if$rebuildOverriddenFontStorages(CallbackInfo ci) {
        this.if$rebuildOverriddenFontStorages();
    }

    @ModifyArg(method = {"createTextRenderer", "createAdvanceValidatingTextRenderer"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;<init>(Ljava/util/function/Function;Z)V"))
    private Function<Identifier, FontStorage> if$overrideFontStorage(Function<Identifier, FontStorage> original) {
        return id -> {
            // Fast path for default font
            if (MinecraftClient.DEFAULT_FONT_ID.equals(id) && this.defaultFontStorage != null) {
                return this.defaultFontStorage;
            } else if (MinecraftClient.UNICODE_FONT_ID.equals(id) && this.unicodeFontStorage != null) {
                return this.unicodeFontStorage;
            }

            // Try to get the font storage from the overridden map otherwise
            final FontStorage storage = this.overriddenFontStorages.get(id);
            if (storage != null) {
                return storage;
            }

            // In case some mod is doing cursed stuff call the original function
            return original.apply(id);
        };
    }

    @Unique
    private void if$rebuildOverriddenFontStorages() {
        this.overriddenFontStorages.clear();
        this.overriddenFontStorages.putAll(this.fontStorages);
        for (Identifier key : this.idOverrides.keySet()) {
            this.overriddenFontStorages.put(key, this.method_27542(key));
        }

        this.defaultFontStorage = this.overriddenFontStorages.get(MinecraftClient.DEFAULT_FONT_ID);
        this.unicodeFontStorage = this.overriddenFontStorages.get(MinecraftClient.UNICODE_FONT_ID);
    }

}
