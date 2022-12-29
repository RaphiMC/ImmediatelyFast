package net.raphimc.immediatelyfast.injection.mixins.batching;

import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DebugHud.class, priority = 2000)
public abstract class MixinDebugHud {

    @Inject(method = {"renderLeftText", "renderRightText"}, at = @At("HEAD"))
    private void beginBatching(MatrixStack matrices, CallbackInfo ci) {
        BatchingBuffers.beginHudBatching();
    }

    @Inject(method = {"renderLeftText", "renderRightText"}, at = @At("RETURN"))
    private void endBatching(MatrixStack matrices, CallbackInfo ci) {
        BatchingBuffers.endHudBatching();
    }

}
