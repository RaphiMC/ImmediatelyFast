package net.raphimc.immediatelyfast.injection.mixins;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.GlAllocationUtils;
import net.raphimc.immediatelyfast.injection.interfaces.IBufferBuilder;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements IBufferBuilder {

    @Shadow
    private ByteBuffer buffer;

    @Override
    public boolean isReleased() {
        return this.buffer == null;
    }

    @Override
    public void release() {
        if (!this.isReleased()) {
            GlAllocationUtils.ALLOCATOR.free(MemoryUtil.memAddress0(this.buffer));
            this.buffer = null;
        }
    }

}
