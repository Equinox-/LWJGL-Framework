package com.pi.core.buffers;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class GLIntBuffer extends GLBuffer<IntBuffer, GLIntBuffer> {
	public GLIntBuffer(int size) {
		this(new int[size]);
	}

	public GLIntBuffer(int[] data) {
		super(BufferUtils.createIntBuffer(data.length).put(data));
	}

	public void get(int buffOffset, int[] data, int offset, int length) {
		super.data.position(buffOffset);
		super.data.get(data, offset, length);
	}

	public int get(int index) {
		return super.data.get(index);
	}

	public void put(int buffOffset, int[] data, int offset, int length) {
		super.data.position(buffOffset);
		super.data.put(data, offset, length);
	}

	public void put(int index, int f) {
		super.data.put(index, f);
	}

	@Override
	protected IntBuffer genBuffer(int size) {
		return BufferUtils.createIntBuffer(size);
	}

	@Override
	protected void glGetBufferSubData(int target, long offset, IntBuffer data) {
		GL15.glGetBufferSubData(target, offset, data);
	}

	@Override
	protected void glBufferSubData(int target, long offset, IntBuffer data) {
		GL15.glBufferSubData(target, offset, data);
	}
}
