package net.raphimc.immediatelyfast.injection.mixins.core;

import net.minecraft.client.gui.hud.DebugHud;
import net.raphimc.immediatelyfast.feature.core.BufferBuilderPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = DebugHud.class, priority = 9999)
public abstract class MixinDebugHud {

    @Inject(method = "getRightText", at = @At("RETURN"))
    private void appendAllocationInfo(CallbackInfoReturnable<List<String>> cir) {
        cir.getReturnValue().add("");
        cir.getReturnValue().add("ImmediatelyFast");
        cir.getReturnValue().add("Buffer Pool: " + BufferBuilderPool.getAllocatedSize());
    }

}
