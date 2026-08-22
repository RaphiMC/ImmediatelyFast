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
package net.raphimc.immediatelyfast.injection.mixins.enhanced_batching;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.raphimc.immediatelyfast.injection.interfaces.IGuiItemAtlas;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(GuiRenderer.class)
public abstract class MixinGuiRenderer {

    @Shadow
    @Final
    private GuiRenderState renderState;

    @Shadow
    @Final
    private FeatureRenderDispatcher featureRenderDispatcher;

    @Unique
    private GuiItemAtlas immediatelyFast$animatedItemAtlas;

    @Unique
    private int immediatelyFast$animatedSlotTextureSize;

    @Inject(method = "prepareItemElements", at = @At("HEAD"))
    private void prepareAnimatedItemAtlas(final CallbackInfo ci) {
        final Set<Object> animatedItemModelIdentities = new HashSet<>();
        this.renderState.forEachItem(itemRenderState -> {
            final TrackingItemStackRenderState itemStackRenderState = itemRenderState.itemStackRenderState();
            if (itemRenderState.oversizedItemBounds() == null && itemStackRenderState.isAnimated()) {
                animatedItemModelIdentities.add(itemStackRenderState.getModelIdentity());
            }
        });

        if (animatedItemModelIdentities.isEmpty()) {
            this.immediatelyFast$closeAnimatedItemAtlas();
            return;
        }

        final int guiScale = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.guiScale;
        final int slotTextureSize = GuiRenderer.DEFAULT_ITEM_SIZE * guiScale;
        final int textureSize = GuiItemAtlas.computeTextureSizeFor(slotTextureSize, animatedItemModelIdentities.size());
        if (this.immediatelyFast$animatedItemAtlas == null
                || this.immediatelyFast$animatedSlotTextureSize != slotTextureSize
                || this.immediatelyFast$animatedItemAtlas.textureSize() != textureSize) {
            this.immediatelyFast$closeAnimatedItemAtlas();
            this.immediatelyFast$animatedItemAtlas = new GuiItemAtlas(this.featureRenderDispatcher, textureSize, slotTextureSize);
            this.immediatelyFast$animatedSlotTextureSize = slotTextureSize;
        } else {
            ((IGuiItemAtlas) this.immediatelyFast$animatedItemAtlas).immediatelyFast$reset();
        }
    }

    @Redirect(method = "lambda$prepareItemElements$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiItemAtlas;getOrUpdate(Lnet/minecraft/client/renderer/item/TrackingItemStackRenderState;)Lnet/minecraft/client/gui/render/GuiItemAtlas$SlotView;"))
    private GuiItemAtlas.SlotView useAnimatedItemAtlas(final GuiItemAtlas itemAtlas, final TrackingItemStackRenderState itemStackRenderState) {
        if (itemStackRenderState.isAnimated() && this.immediatelyFast$animatedItemAtlas != null) {
            return this.immediatelyFast$animatedItemAtlas.getOrUpdate(itemStackRenderState);
        }
        return itemAtlas.getOrUpdate(itemStackRenderState);
    }

    @Inject(method = "endFrame", at = @At("TAIL"))
    private void endAnimatedItemAtlasFrame(final CallbackInfo ci) {
        if (this.immediatelyFast$animatedItemAtlas != null) {
            this.immediatelyFast$animatedItemAtlas.endFrame();
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void closeAnimatedItemAtlas(final CallbackInfo ci) {
        this.immediatelyFast$closeAnimatedItemAtlas();
    }

    @Unique
    private void immediatelyFast$closeAnimatedItemAtlas() {
        if (this.immediatelyFast$animatedItemAtlas != null) {
            this.immediatelyFast$animatedItemAtlas.close();
            this.immediatelyFast$animatedItemAtlas = null;
            this.immediatelyFast$animatedSlotTextureSize = 0;
        }
    }

}
