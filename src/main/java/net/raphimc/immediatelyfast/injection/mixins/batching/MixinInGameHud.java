package net.raphimc.immediatelyfast.injection.mixins.batching;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.JumpingMount;
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

    @Inject(method = "renderExperienceBar", at = @At("HEAD"))
    private void beginExperienceBarBatching(MatrixStack matrices, int x, CallbackInfo ci) {
        BatchingBuffers.beginHudBatching();
    }

    @Inject(method = "renderExperienceBar", at = @At("RETURN"))
    private void endExperienceBarBatching(MatrixStack matrices, int x, CallbackInfo ci) {
        BatchingBuffers.endHudBatching();
    }

    @Inject(method = "renderMountJumpBar", at = @At("HEAD"))
    private void beginMountJumpBarBatching(JumpingMount mount, MatrixStack matrices, int x, CallbackInfo ci) {
        BatchingBuffers.beginHudBatching();
    }

    @Inject(method = "renderMountJumpBar", at = @At("RETURN"))
    private void endMountJumpBarBatching(JumpingMount mount, MatrixStack matrices, int x, CallbackInfo ci) {
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

    /*@Inject(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFI)I", shift = At.Shift.BEFORE, ordinal = 4))
    private void test(MatrixStack matrices, int x, CallbackInfo ci) {
        matrices.translate(0, 0, 0.05);
    }

    public static void test(MatrixStack matrices) {
    beginFillBatching();
    DrawableHelper.fill(matrices, 50, 50, 100, 100, Color.black.getRGB());
    DrawableHelper.fill(matrices, 150, 150, 200, 200, Color.black.getRGB());
    DrawableHelper.fill(matrices, 75, 75, 175, 175, Color.red.getRGB());
    endFillBatching();

    matrices.translate(200, 0, 0);
    DrawableHelper.fill(matrices, 50, 50, 100, 100, Color.black.getRGB());
    DrawableHelper.fill(matrices, 150, 150, 200, 200, Color.black.getRGB());
    DrawableHelper.fill(matrices, 75, 75, 175, 175, Color.red.getRGB());
    }
    */

}
