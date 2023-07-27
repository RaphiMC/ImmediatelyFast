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

import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PersistentMappedStreamingBuffer {

    private final int id;
    private final long size;
    private final long addr;
    private final List<Batch> batches = new ArrayList<>();

    private long offset;

    public PersistentMappedStreamingBuffer(final long size) {
        this.id = GL45C.glCreateBuffers();
        this.size = size;
        GL45C.glNamedBufferStorage(this.id, size, GL30C.GL_MAP_WRITE_BIT | GL44C.GL_MAP_PERSISTENT_BIT | GL44C.GL_MAP_COHERENT_BIT | GL44C.GL_CLIENT_STORAGE_BIT);
        this.addr = GL45C.nglMapNamedBufferRange(this.id, 0L, size, GL30C.GL_MAP_WRITE_BIT | GL44C.GL_MAP_PERSISTENT_BIT | GL44C.GL_MAP_COHERENT_BIT | GL30C.GL_MAP_INVALIDATE_RANGE_BIT);
    }

    public void addUpload(final int destinationId, final ByteBuffer data) {
        final int dataSize = data.remaining();
        if (dataSize > this.size) {
            throw new RuntimeException("Data size is bigger than buffer size");
        }
        if (this.offset + dataSize > this.size) {
            this.flush();
            GL11C.glFinish();
            this.offset = 0;
        }

        MemoryUtil.memCopy(MemoryUtil.memAddress(data), this.addr + this.offset, dataSize);
        this.batches.add(new Batch(destinationId, this.offset, dataSize));
        this.offset += dataSize;
    }

    public void flush() {
        if (this.batches.isEmpty()) return;

        for (Batch batch : this.batches) {
            GL45C.glCopyNamedBufferSubData(this.id, batch.destinationId, batch.offset, 0, batch.size);
        }
        GL42C.glMemoryBarrier(GL42C.GL_BUFFER_UPDATE_BARRIER_BIT);
        this.batches.clear();
    }

    public long getSize() {
        return this.size;
    }

    public long getOffset() {
        return this.offset;
    }

    private record Batch(int destinationId, long offset, int size) {
    }

}
