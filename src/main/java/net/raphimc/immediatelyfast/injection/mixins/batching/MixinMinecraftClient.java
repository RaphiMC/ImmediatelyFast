package net.raphimc.immediatelyfast.injection.mixins.batching;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.profiler.ProfileResult;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftClient.class, priority = 2000)
public abstract class MixinMinecraftClient {

    @Inject(method = "drawProfilerResults", at = @At("HEAD"))
    private void beginBatching(MatrixStack matrices, ProfileResult profileResult, CallbackInfo ci) {
        BatchingBuffers.beginHudBatching();
    }

    @Inject(method = "drawProfilerResults", at = @At("RETURN"))
    private void endBatching(MatrixStack matrices, ProfileResult profileResult, CallbackInfo ci) {
        BatchingBuffers.endHudBatching();
    }

}
