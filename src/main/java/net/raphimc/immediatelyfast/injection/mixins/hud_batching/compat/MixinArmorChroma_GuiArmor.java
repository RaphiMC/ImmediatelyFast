package net.raphimc.immediatelyfast.injection.mixins.hud_batching.compat;

import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "nukeduck.armorchroma.GuiArmor")
@Pseudo
public abstract class MixinArmorChroma_GuiArmor {

    @Unique
    private boolean wasTextureBatching;

    @Inject(method = "drawMaskedIcon", at = @At("HEAD"))
    private void if$endTextureBatching(CallbackInfo ci) {
        if (BatchingBuffers.isTextureBatching()) {
            BatchingBuffers.endTextureBatching();
            this.wasTextureBatching = true;
        }
    }

    @Inject(method = "drawMaskedIcon", at = @At("RETURN"))
    private void if$beginTextureBatching(CallbackInfo ci) {
        if (this.wasTextureBatching) {
            BatchingBuffers.beginTextureBatching();
            this.wasTextureBatching = false;
        }
    }

}
