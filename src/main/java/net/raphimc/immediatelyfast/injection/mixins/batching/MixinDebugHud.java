package net.raphimc.immediatelyfast.injection.mixins.batching;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DebugHud.class, priority = 500)
public abstract class MixinDebugHud {

    @WrapOperation(method = "render", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;renderLeftText(Lnet/minecraft/client/util/math/MatrixStack;)V"),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;renderRightText(Lnet/minecraft/client/util/math/MatrixStack;)V"),
    })
    private void if$batching(final DebugHud instance, final MatrixStack matrices, final Operation<MatrixStack> operation) {
        BatchingBuffers.beginHudBatching();
        operation.call(instance, matrices);
        BatchingBuffers.endHudBatching();
    }

}
