package com.pi.core.buffers;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class GLFloatBuffer extends GLBuffer<FloatBuffer, GLFloatBuffer> {

	public GLFloatBuffer(int size) {
		this(new float[size]);
	}

	public GLFloatBuffer(float[] data) {
		super(BufferUtils.createFloatBuffer(data.length).put(data));
	}

	public void get(int buffOffset, float[] data, int offset, int length) {
		super.data.position(buffOffset);
		super.data.get(data, offset, length);
	}

	public float get(int index) {
		return super.data.get(index);
	}

	public void put(int buffOffset, float[] data, int offset, int length) {
		super.data.position(buffOffset);
		super.data.put(data, offset, length);
	}

	public void put(int index, float f) {
		super.data.put(index, f);
	}

	@Override
	protected FloatBuffer genBuffer(int size) {
		return BufferUtils.createFloatBuffer(size);
	}

	@Override
	protected void glGetBufferSubData(int target, long offset, FloatBuffer data) {
		GL15.glGetBufferSubData(target, offset, data);
	}

	@Override
	protected void glBufferSubData(int target, long offset, FloatBuffer data) {
		GL15.glBufferSubData(target, offset, data);
	}
}
