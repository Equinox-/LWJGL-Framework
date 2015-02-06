package com.pi.core.buffers;

import java.nio.Buffer;

import org.lwjgl.opengl.GL15;

import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GPUObject;

abstract class GLBuffer<E extends Buffer, R extends GLBuffer<E, R>> implements
		GLIdentifiable, GPUObject {
	private static final int[][] HINT_TABLE;
	static {
		BufferAccessHint[] ahv = BufferAccessHint.values();
		BufferModifyHint[] mhv = BufferModifyHint.values();
		HINT_TABLE = new int[ahv.length][mhv.length];
		for (int a = 0; a < ahv.length; a++) {
			for (int m = 0; m < mhv.length; m++) {
				try {
					HINT_TABLE[a][m] = GL15.class.getField(
							"GL_" + mhv[m].name() + "_" + ahv[a].name())
							.getInt(null);
				} catch (Exception e) {
					throw new RuntimeException(
							"Illegal lookup when generating hint table", e);
				}
			}
		}
	}

	private BufferAccessHint accessHint;
	private BufferModifyHint modifyHint;

	private final int size;
	protected E data;
	private int bufferPtr;

	public GLBuffer(E data) {
		this.size = data.capacity();
		this.data = data;
		this.bufferPtr = -1;
	}

	@SuppressWarnings("unchecked")
	public R access(BufferAccessHint a) {
		if (bufferPtr != -1)
			throw new RuntimeException(
					"Can't change buffer hints while allocated on the GPU");
		this.accessHint = a;
		return (R) this;
	}

	@SuppressWarnings("unchecked")
	public R modify(BufferModifyHint a) {
		if (bufferPtr != -1)
			throw new RuntimeException(
					"Can't change buffer hints while allocated on the GPU");
		this.modifyHint = a;
		return (R) this;
	}

	@Override
	public void gpuAlloc() {
		if (bufferPtr != -1)
			gpuFree();
		bufferPtr = GL15.glGenBuffers();

		int ahI = accessHint == null ? 0 : accessHint.ordinal();
		int mhI = modifyHint == null ? 0 : modifyHint.ordinal();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferPtr);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, size, HINT_TABLE[ahI][mhI]);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	@Override
	public void gpuFree() {
		if (bufferPtr >= 0)
			GL15.glDeleteBuffers(bufferPtr);
		bufferPtr = -1;
	}

	public void cpuAlloc() {
		if (data == null)
			data = genBuffer(size);
	}

	public void cpuFree() {
		data = null;
	}

	public void syncToGPU() {
		if (bufferPtr == -1)
			throw new RuntimeException(
					"Can't sync to GPU when no buffer object exists.");
		data.position(0);
		data.limit(data.capacity());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferPtr);
		glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public void syncFromGPU() {
		if (bufferPtr == -1)
			throw new RuntimeException(
					"Can't sync from GPU when no buffer object exists.");
		if (data == null)
			data = genBuffer(size);
		data.position(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferPtr);
		glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	public void dispose() {
		gpuFree();
		cpuFree();
	}

	protected abstract E genBuffer(int size);

	protected abstract void glGetBufferSubData(int target, long offset, E data);

	protected abstract void glBufferSubData(int target, long offset, E data);

	@Override
	public int getID() {
		return bufferPtr;
	}

	public int size() {
		return size;
	}

	public E getBacking() {
		return data;
	}
}
