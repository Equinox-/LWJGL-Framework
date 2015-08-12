package com.pi.core.buffers;

import java.nio.Buffer;

import org.lwjgl.opengl.GL15;

import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GPUObject;

abstract class GLBuffer<E extends Buffer, R extends GLBuffer<E, R>> extends
		GPUObject<R> implements GLIdentifiable {
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

	private int size;
	private BufferType type;
	protected E data;
	private int bufferPtr;

	public GLBuffer(E data) {
		this(data, BufferType.ARRAY);
	}

	public GLBuffer(E data, BufferType type) {
		this.size = data.capacity();
		this.data = data;
		this.bufferPtr = -1;
		this.type = type;

		this.accessHint = BufferAccessHint.DRAW;
		this.modifyHint = BufferModifyHint.STATIC;
	}

	@SuppressWarnings("unchecked")
	public R access(BufferAccessHint a) {
		if (a == null)
			throw new IllegalArgumentException(
					"The buffer access hint must not be null.");
		if (bufferPtr != -1)
			throw new RuntimeException(
					"Can't change buffer hints while allocated on the GPU");
		this.accessHint = a;
		return (R) this;
	}

	@SuppressWarnings("unchecked")
	public R modify(BufferModifyHint a) {
		if (a == null)
			throw new IllegalArgumentException(
					"The buffer modify hint must not be null.");
		if (bufferPtr != -1)
			throw new RuntimeException(
					"Can't change buffer hints while allocated on the GPU");
		this.modifyHint = a;
		return (R) this;
	}

	private void allocBufferStorage() {
		int ahI = accessHint.ordinal();
		int mhI = modifyHint.ordinal();
		GL15.glBindBuffer(type.code(), bufferPtr);
		GL15.glBufferData(type.code(), size, HINT_TABLE[ahI][mhI]);
		GL15.glBindBuffer(type.code(), 0);
	}

	@Override
	protected void gpuAllocInternal() {
		if (bufferPtr != -1)
			gpuFreeInternal();
		bufferPtr = GL15.glGenBuffers();
		allocBufferStorage();
	}

	@Override
	protected void gpuFreeInternal() {
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

	@Override
	protected void gpuUploadInternal() {
		if (bufferPtr == -1)
			throw new RuntimeException(
					"Can't sync to GPU when no buffer object exists.");
		data.position(0);
		data.limit(data.capacity());
		GL15.glBindBuffer(type.code(), bufferPtr);
		glBufferSubData(type.code(), 0, data);
		GL15.glBindBuffer(type.code(), 0);
	}

	public void gpuUploadPartial(int min, int max) {
		if (bufferPtr == -1)
			throw new RuntimeException(
					"Can't sync to GPU when no buffer object exists.");
		data.position(min);
		data.limit(max);
		GL15.glBindBuffer(type.code(), bufferPtr);
		glBufferSubData(type.code(), min, data);
		GL15.glBindBuffer(type.code(), 0);
	}

	@Override
	protected void gpuDownloadInternal() {
		if (bufferPtr == -1)
			throw new RuntimeException(
					"Can't sync from GPU when no buffer object exists.");
		if (data == null)
			cpuAlloc();
		data.position(0);
		GL15.glBindBuffer(type.code(), bufferPtr);
		glGetBufferSubData(type.code(), 0, data);
		GL15.glBindBuffer(type.code(), 0);
	}

	public void dispose() {
		gpuFreeInternal();
		cpuFree();
	}

	protected abstract E genBuffer(int size);

	protected abstract void glGetBufferSubData(int target, long offset, E data);

	protected abstract void glBufferSubData(int target, long offset, E data);

	@Override
	public int getID() {
		return bufferPtr;
	}

	/**
	 * Resizes the buffer. Warning: This DOES NOT preserve buffer contents. Can
	 * be run even if buffer is allocated.
	 * 
	 * @param ns
	 *            the new size
	 * @return this buffer
	 */
	@SuppressWarnings("unchecked")
	public R resize(int ns) {
		if (size != ns) {
			this.size = ns;
			if (data != null)
				data = genBuffer(ns);
			if (allocated())
				allocBufferStorage();
		}
		return (R) this;
	}

	public int size() {
		return size;
	}

	@SuppressWarnings("unchecked")
	public R type(BufferType t) {
		if (bufferPtr != -1)
			throw new IllegalStateException(
					"Can't change buffer type when allocated.");
		this.type = t;
		return (R) this;
	}

	public BufferType type() {
		return type;
	}

	public E getBacking() {
		return data;
	}
}
