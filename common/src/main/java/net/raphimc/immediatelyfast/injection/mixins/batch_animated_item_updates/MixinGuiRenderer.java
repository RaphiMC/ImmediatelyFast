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
package net.raphimc.immediatelyfast.injection.mixins.batch_animated_item_updates;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.raphimc.immediatelyfast.feature.batch_animated_item_updates.AnimatedItemAtlas;
import net.raphimc.immediatelyfast.injection.interfaces.IGuiRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(GuiRenderer.class)
public abstract class MixinGuiRenderer implements IGuiRenderer {

    @Shadow
    @Final
    private GuiRenderState renderState;

    @Shadow
    @Final
    private FeatureRenderDispatcher featureRenderDispatcher;

    @Shadow
    private int cachedGuiScale;

    @Unique
    private AnimatedItemAtlas immediatelyFast$animatedItemAtlas;

    @Unique
    private boolean immediatelyFast$useAnimatedItemAtlas;

    @Inject(method = "prepareItemElements", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;prepareItemAtlas(Ljava/util/Set;I)Lnet/minecraft/client/gui/render/GuiItemAtlas;"))
    private void prepareAnimatedItemAtlas(CallbackInfo ci) {
        final ObjectSet<Object> animatedItemModelIdentities = new ObjectOpenHashSet<>();
        this.renderState.forEachItem(itemRenderState -> {
            final TrackingItemStackRenderState itemStackRenderState = itemRenderState.itemStackRenderState();
            if (itemRenderState.oversizedItemBounds() == null && itemStackRenderState.isAnimated()) {
                animatedItemModelIdentities.add(itemStackRenderState.getModelIdentity());
            }
        });
        this.immediatelyFast$useAnimatedItemAtlas = this.immediatelyFast$prepareAnimatedItemAtlas(animatedItemModelIdentities, GuiRenderer.DEFAULT_ITEM_SIZE * this.cachedGuiScale);
    }

    @ModifyReceiver(method = "lambda$prepareItemElements$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiItemAtlas;getOrUpdate(Lnet/minecraft/client/renderer/item/TrackingItemStackRenderState;)Lnet/minecraft/client/gui/render/GuiItemAtlas$SlotView;"))
    private GuiItemAtlas useAnimatedItemAtlas(GuiItemAtlas instance, TrackingItemStackRenderState item) {
        if (this.immediatelyFast$useAnimatedItemAtlas && item.isAnimated()) {
            return this.immediatelyFast$animatedItemAtlas;
        } else {
            return instance;
        }
    }

    @Inject(method = "endFrame", at = @At("RETURN"))
    private void endAnimatedItemAtlasFrame(final CallbackInfo ci) {
        if (this.immediatelyFast$animatedItemAtlas != null) {
            this.immediatelyFast$animatedItemAtlas.endFrame();
        }
    }

    @Inject(method = "invalidateItemAtlas", at = @At("RETURN"))
    private void invalidateAnimatedItemAtlas(final CallbackInfo ci) {
        this.immediatelyFast$closeAnimatedItemAtlas();
    }

    @Inject(method = "close", at = @At("RETURN"))
    private void closeAnimatedItemAtlas(final CallbackInfo ci) {
        this.immediatelyFast$closeAnimatedItemAtlas();
    }

    @Unique
    private boolean immediatelyFast$prepareAnimatedItemAtlas(final Set<Object> animatedItemsInFrame, final int slotTextureSize) {
        if (animatedItemsInFrame.size() < 3) {
            return false; // Don't use the animated item atlas if there are too few animated items
        }
        final int newTextureSize = AnimatedItemAtlas.computeTextureSizeFor(slotTextureSize, animatedItemsInFrame.size());
        if (this.immediatelyFast$animatedItemAtlas == null || newTextureSize > this.immediatelyFast$animatedItemAtlas.textureSize() || newTextureSize < this.immediatelyFast$animatedItemAtlas.textureSize() / 4) {
            this.immediatelyFast$closeAnimatedItemAtlas();
            this.immediatelyFast$animatedItemAtlas = new AnimatedItemAtlas(this.featureRenderDispatcher, newTextureSize, slotTextureSize);
        }
        return this.immediatelyFast$animatedItemAtlas.tryPrepareFor(animatedItemsInFrame); // Return if all items fit in the atlas
    }

    @Unique
    private void immediatelyFast$closeAnimatedItemAtlas() {
        if (this.immediatelyFast$animatedItemAtlas != null) {
            this.immediatelyFast$animatedItemAtlas.close();
            this.immediatelyFast$animatedItemAtlas = null;
        }
    }

    @Override
    public AnimatedItemAtlas immediatelyFast$getAnimatedItemAtlas() {
        return this.immediatelyFast$animatedItemAtlas;
    }

}
