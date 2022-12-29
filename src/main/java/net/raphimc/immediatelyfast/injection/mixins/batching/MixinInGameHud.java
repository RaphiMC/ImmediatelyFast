package net.raphimc.immediatelyfast.injection.mixins.batching;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InGameHud.class, priority = 2000)
public abstract class MixinInGameHud {

    @Inject(method = {"renderStatusBars", "renderMountHealth", "renderStatusEffectOverlay", "renderCrosshair"}, at = @At("HEAD"))
    private void beginGeneralBatching(MatrixStack matrices, CallbackInfo ci) {
        BatchingBuffers.beginHudBatching();
    }

    @Inject(method = {"renderStatusBars", "renderMountHealth", "renderStatusEffectOverlay", "renderCrosshair"}, at = @At("RETURN"))
    private void endGeneralBatching(MatrixStack matrices, CallbackInfo ci) {
        BatchingBuffers.endHudBatching();
    }

    // TODO: Z layering issues
    /*@Inject(method = "renderExperienceBar", at = @At("HEAD"))
    private void beginExperienceBarBatching(MatrixStack matrices, int x, CallbackInfo ci) {
        BatchingBuffers.beginHudBatching();
    }

    @Inject(method = "renderExperienceBar", at = @At("RETURN"))
    private void endExperienceBarBatching(MatrixStack matrices, int x, CallbackInfo ci) {
        BatchingBuffers.endHudBatching();
    }*/

    @Inject(method = "renderMountJumpBar", at = @At("HEAD"))
    private void beginMountJumpBarBatching(MatrixStack matrices, int x, CallbackInfo ci) {
        BatchingBuffers.beginHudBatching();
    }

    @Inject(method = "renderMountJumpBar", at = @At("RETURN"))
    private void endMountJumpBarBatching(MatrixStack matrices, int x, CallbackInfo ci) {
        BatchingBuffers.endHudBatching();
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"))
    private void beginScoreboardSidebarBatching(MatrixStack matrices, ScoreboardObjective objective, CallbackInfo ci) {
        BatchingBuffers.beginHudBatching();
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("RETURN"))
    private void endScoreboardSidebarBatching(MatrixStack matrices, ScoreboardObjective objective, CallbackInfo ci) {
        BatchingBuffers.endHudBatching();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"))
    private void beginHotbarBatching(float tickDelta, MatrixStack matrices, CallbackInfo ci) {
        //BatchingBuffers.beginHudBatching(); // TODO: Z layering issues
        BatchingBuffers.beginItemBatching();
    }

    @Inject(method = "renderHotbar", at = @At("RETURN"))
    private void endHotbarBatching(float tickDelta, MatrixStack matrices, CallbackInfo ci) {
        //BatchingBuffers.endHudBatching();
        BatchingBuffers.endItemBatching();
    }

}
