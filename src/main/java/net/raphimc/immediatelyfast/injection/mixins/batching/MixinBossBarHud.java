package net.raphimc.immediatelyfast.injection.mixins.batching;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.util.math.MatrixStack;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BossBarHud.class, priority = 2000)
public abstract class MixinBossBarHud {

    @Inject(method = "render", at = @At("HEAD"))
    private void beginBatching(MatrixStack matrices, CallbackInfo ci) {
        BatchingBuffers.beginHudBatching();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void endBatching(MatrixStack matrices, CallbackInfo ci) {
        BatchingBuffers.endHudBatching();
    }

}
