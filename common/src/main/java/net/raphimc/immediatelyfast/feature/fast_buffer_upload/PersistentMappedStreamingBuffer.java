/*
 * This file is part of ImmediatelyFast - https://github.com/RaphiMC/ImmediatelyFast
 * Copyright (C) 2023 RK_01/RaphiMC and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.immediatelyfast.feature.fast_buffer_upload;

import net.raphimc.immediatelyfast.ImmediatelyFast;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersistentMappedStreamingBuffer {

    private final int id;
    private final long size;
    private final long addr;
    private final long syncSectionSize;

    private final List<Batch> batches = new ArrayList<>();
    private final long[] fences = new long[8];
    private long batchOffset;
    private long offset;

    public PersistentMappedStreamingBuffer(final long size) {
        this.id = GL45C.glCreateBuffers();
        this.size = size;
        this.syncSectionSize = size / this.fences.length;
        Arrays.fill(this.fences, -1L);

        int flags = GL30C.GL_MAP_WRITE_BIT | GL44C.GL_MAP_PERSISTENT_BIT;
        if (ImmediatelyFast.config.fast_buffer_upload_explicit_flush) {
            flags |= GL30C.GL_MAP_FLUSH_EXPLICIT_BIT;
        } else {
            flags |= GL44C.GL_MAP_COHERENT_BIT;
        }

        GL45C.glNamedBufferStorage(this.id, size, (flags & ~GL30C.GL_MAP_FLUSH_EXPLICIT_BIT) | GL44C.GL_CLIENT_STORAGE_BIT);
        this.addr = GL45C.nglMapNamedBufferRange(this.id, 0L, size, flags | GL30C.GL_MAP_UNSYNCHRONIZED_BIT | GL30C.GL_MAP_INVALIDATE_RANGE_BIT);
    }

    public void addUpload(final int destinationId, final ByteBuffer data) {
        final int dataSize = data.remaining();
        if (dataSize > this.size) {
            throw new RuntimeException("Data size is bigger than buffer size");
        }
        if (dataSize <= 0) {
            throw new RuntimeException("Data is empty");
        }

        int oldFenceIdx = (int) (this.offset / this.syncSectionSize);
        if (oldFenceIdx >= this.fences.length) {
            oldFenceIdx = this.fences.length - 1;
        }

        if (this.offset + dataSize > this.size) {
            this.flush();
            if (dataSize >= this.offset) {
                final long fence = this.fences[oldFenceIdx];
                if (fence != -1) {
                    GL32C.glClientWaitSync(fence, GL32C.GL_SYNC_FLUSH_COMMANDS_BIT, GL32C.GL_TIMEOUT_IGNORED);
                } else {
                    GL32C.glFinish();
                }
            } else if (this.fences[oldFenceIdx] == -1) {
                this.fences[oldFenceIdx] = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
            }

            oldFenceIdx = -1;
            this.offset = 0;
            this.batchOffset = 0;
        }

        final long newOffset = this.offset + dataSize;
        final int newFenceIdx = (int) ((newOffset - 1) / this.syncSectionSize);

        long fence = -1;
        for (int i = newFenceIdx; i > oldFenceIdx && fence == -1; i--) {
            fence = this.fences[i];
        }
        if (fence != -1) {
            GL32C.glClientWaitSync(fence, GL32C.GL_SYNC_FLUSH_COMMANDS_BIT, GL32C.GL_TIMEOUT_IGNORED);
        }

        MemoryUtil.memCopy(MemoryUtil.memAddress(data), this.addr + this.offset, dataSize);
        this.batches.add(new Batch(destinationId, dataSize));
        this.offset = newOffset;
    }

    public void flush() {
        if (this.batches.isEmpty()) return;

        if (ImmediatelyFast.config.fast_buffer_upload_explicit_flush) {
            GL45C.glFlushMappedNamedBufferRange(this.id, this.batchOffset, this.offset - this.batchOffset);
            GL42C.glMemoryBarrier(GL44C.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT);
        }
        final int oldFenceIdx = (int) (this.batchOffset / this.syncSectionSize);
        for (Batch batch : this.batches) {
            GL45C.glCopyNamedBufferSubData(this.id, batch.destinationId, this.batchOffset, 0, batch.size);
            this.batchOffset += batch.size;
        }
        this.batches.clear();
        GL42C.glMemoryBarrier(GL42C.GL_BUFFER_UPDATE_BARRIER_BIT);

        final int nextFenceIdx = (int) (this.batchOffset / this.syncSectionSize);
        final int newFenceIdx = (int) ((this.batchOffset - 1) / this.syncSectionSize);
        for (int i = oldFenceIdx; i <= newFenceIdx; i++) {
            final long fence = this.fences[i];
            if (fence != -1) {
                GL32C.glDeleteSync(fence);
                this.fences[i] = -1;
            }
        }

        if (oldFenceIdx != nextFenceIdx) {
            this.fences[oldFenceIdx] = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
        }
    }

    public long getSize() {
        return this.size;
    }

    public long getOffset() {
        return this.offset;
    }

    private record Batch(int destinationId, int size) {
    }

}
