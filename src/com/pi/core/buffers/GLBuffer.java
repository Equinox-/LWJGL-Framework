package com.pi.core.buffers;

import java.nio.Buffer;

import org.lwjgl.opengl.GL15;

import com.pi.core.debug.FrameCounter;
import com.pi.core.debug.FrameCounter.FrameParam;
import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GPUObject;
import com.pi.core.util.GLRef;
import com.pi.util.ReferenceTable;

abstract class GLBuffer<E extends Buffer, R extends GLBuffer<E, R>> extends GPUObject<R> implements GLIdentifiable {
	private static final int[][] HINT_TABLE;
	@SuppressWarnings("rawtypes")
	private static final ReferenceTable<GLBuffer> bound = new ReferenceTable<>(1);

	static {
		BufferAccessHint[] ahv = BufferAccessHint.values();
		BufferModifyHint[] mhv = BufferModifyHint.values();
		HINT_TABLE = new int[ahv.length][mhv.length];
		for (int a = 0; a < ahv.length; a++) {
			for (int m = 0; m < mhv.length; m++)

			{
				String constName = "GL_" + mhv[m].name() + "_" + ahv[a].name();
				try {
					HINT_TABLE[a][m] = GL15.class.getField(constName).getInt(null);
				} catch (Exception e) {
					throw new UnsupportedOperationException(
							"Illegal lookup when generating hint table: GL15." + constName, e);
				}
			}
		}
	}

	private BufferAccessHint accessHint;
	private BufferModifyHint modifyHint;

	private int size;
	private BufferType type;
	protected E data;
	private int glref;

	public static void unbind(BufferType type) {
		if (bound.isEmpty(0))
			return;
		GL15.glBindBuffer(type.code(), 0);
		FrameCounter.increment(FrameParam.BUFFER_BINDS);
		bound.empty(0);
	}

	public GLBuffer(E data) {
		this(data, BufferType.ARRAY);
	}

	public GLBuffer(E data, BufferType type) {
		this.size = data.capacity();
		this.data = data;
		this.glref = GLRef.NULL;
		this.type = type;

		this.accessHint = BufferAccessHint.DRAW;
		this.modifyHint = BufferModifyHint.STATIC;
	}

	@SuppressWarnings("unchecked")
	public R access(BufferAccessHint a) {
		if (a == null)
			throw new IllegalArgumentException("The buffer access hint must not be null.");
		if (GLRef.notNull(glref))
			throw new IllegalStateException("Can't change buffer hints while allocated on the GPU");
		this.accessHint = a;
		return (R) this;
	}

	private void allocBufferStorage() {
		int ahI = accessHint.ordinal();
		int mhI = modifyHint.ordinal();
		bind();
		GL15.glBufferData(type.code(), size, HINT_TABLE[ahI][mhI]);
	}

	public void bind() {
		bind(type);
	}

	public void bind(BufferType type) {
		if (GLRef.isNull(glref))
			throw new IllegalStateException("Can't bind an unallocated buffer");
		if (bound.isAttached(0, this))
			return;
		GL15.glBindBuffer(type.code(), glref);
		FrameCounter.increment(FrameParam.BUFFER_BINDS);
		bound.attach(0, this);
	}

	public void cpuAlloc() {
		if (data == null)
			data = genBuffer(size);
	}

	public void cpuFree() {
		data = null;
	}

	public void dispose() {
		gpuFreeInternal();
		cpuFree();
	}

	protected abstract E genBuffer(int size);

	public E getBacking() {
		return data;
	}

	@Override
	public int getID() {
		return glref;
	}

	protected abstract void glBufferSubData(int target, long offset, E data);

	protected abstract void glGetBufferSubData(int target, long offset, E data);

	@Override
	protected void gpuAllocInternal() {
		glref = GL15.glGenBuffers();
		if (GLRef.isNull(glref))
			throw new NullPointerException("Failed to generate buffer");
		allocBufferStorage();
	}

	@Override
	protected void gpuDownloadInternal() {
		if (GLRef.isNull(glref))
			throw new IllegalStateException("Can't sync from GPU when no buffer object exists.");
		if (data == null)
			cpuAlloc();
		data.position(0);
		bind();
		glGetBufferSubData(type.code(), 0, data);
	}

	@Override
	protected void gpuFreeInternal() {
		if (GLRef.notNull(glref))
			GL15.glDeleteBuffers(glref);
		glref = GLRef.NULL;
	}

	@Override
	protected void gpuUploadInternal() {
		if (GLRef.isNull(glref))
			throw new IllegalStateException("Can't sync to GPU when no buffer object exists.");
		data.position(0);
		data.limit(size);
		bind();
		glBufferSubData(type.code(), 0, data);

		FrameCounter.increment(FrameParam.BUFFER_UPLOADS);
		FrameCounter.increment(FrameParam.BUFFER_THROUGHPUT, data.capacity());
	}

	public void gpuUploadPartial(int minR, int maxR) {
		if (GLRef.isNull(glref))
			throw new IllegalStateException("Can't sync to GPU when no buffer object exists.");
		// Ensure safety.
		int min = Math.max(minR, 0);
		int max = Math.min(maxR, size - 1);
		if (max <= min)
			return;
		data.position(min);
		data.limit(max);
		bind();
		glBufferSubData(type.code(), min, data);

		FrameCounter.increment(FrameParam.BUFFER_UPLOADS);
		FrameCounter.increment(FrameParam.BUFFER_THROUGHPUT, max - min);
	}

	@SuppressWarnings("unchecked")
	public R modify(BufferModifyHint a) {
		if (a == null)
			throw new IllegalArgumentException("The buffer modify hint must not be null.");
		if (GLRef.notNull(glref))
			throw new IllegalStateException("Can't change buffer hints while allocated on the GPU");
		this.modifyHint = a;
		return (R) this;
	}

	/**
	 * Resizes the buffer. Warning: This DOES NOT preserve buffer contents. Can
	 * be run even if buffer is allocated.
	 * 
	 * @param ns
	 *            the new size
	 * @return this buffer
	 */
	public R resize(int ns) {
		return resize(ns, 32);
	}

	/**
	 * Resizes the buffer. Warning: This DOES NOT preserve buffer contents. Can
	 * be run even if buffer is allocated.
	 * 
	 * @param ns
	 *            the new size
	 * @param pad
	 *            Extra padding to add if having to resize the buffer.
	 * @return this buffer
	 */
	@SuppressWarnings("unchecked")
	public R resize(int ns, int pad) {
		if (size != ns) {
			this.size = ns;
			final int cap = ns + pad;
			if (data == null || data.capacity() < ns || data.capacity() > cap) {
				data = genBuffer(cap);
			}
			if (allocated())
				allocBufferStorage();
		}
		return (R) this;
	}

	public int size() {
		return size;
	}

	public BufferType type() {
		return type;
	}

	@SuppressWarnings("unchecked")
	public R type(BufferType t) {
		if (GLRef.notNull(glref))
			throw new IllegalStateException("Can't change buffer type when allocated.");
		this.type = t;
		return (R) this;
	}

	public void unbind() {
		unbind(type);
	}
}
