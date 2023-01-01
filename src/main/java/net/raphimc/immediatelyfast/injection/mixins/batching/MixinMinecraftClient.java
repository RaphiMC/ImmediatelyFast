package net.raphimc.immediatelyfast.injection.mixins.batching;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.profiler.ProfileResult;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import net.raphimc.immediatelyfast.feature.batching.LayeringCorrectingVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftClient.class, priority = 500)
public abstract class MixinMinecraftClient {

    @WrapOperation(method = "render", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;drawProfilerResults(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/profiler/ProfileResult;)V"),
    })
    private void if$Batching(@Coerce final Object instance, final MatrixStack matrices, final ProfileResult profileResult, final Operation<MatrixStack> operation) {
        BatchingBuffers.beginHudBatching();
        operation.call(instance, matrices, profileResult);
        BatchingBuffers.endHudBatching();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;swapBuffers()V", shift = At.Shift.AFTER))
    private void resetZLayeringOffset(boolean tick, CallbackInfo ci) {
        LayeringCorrectingVertexConsumer.resetZOffset();
    }

}
