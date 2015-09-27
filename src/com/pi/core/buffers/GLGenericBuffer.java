package com.pi.core.buffers;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class GLGenericBuffer extends GLBuffer<ByteBuffer, GLGenericBuffer> {
	public GLGenericBuffer(int size) {
		super(BufferUtils.createByteBuffer(size));
	}

	public GLGenericBuffer(ByteBuffer backing) {
		super(backing);
		if (!backing.isDirect())
			throw new RuntimeException("OpenGL buffers must be direct.");
	}

	public ShortBuffer shortImageAt(int i) {
		super.data.limit(super.data.capacity());
		super.data.position(i);
		return super.data.asShortBuffer();
	}

	public IntBuffer integerImageAt(int i) {
		super.data.limit(super.data.capacity());
		super.data.position(i);
		return super.data.asIntBuffer();
	}

	public FloatBuffer floatImageAt(int i) {
		super.data.limit(super.data.capacity());
		super.data.position(i);
		return super.data.asFloatBuffer();
	}

	public void get(int buffOffset, byte[] data, int offset, int length) {
		super.data.limit(super.data.capacity());
		super.data.position(buffOffset);
		super.data.get(data, offset, length);
	}

	public byte get(int index) {
		return super.data.get(index);
	}

	public void put(int buffOffset, byte[] data, int offset, int length) {
		super.data.limit(super.data.capacity());
		super.data.position(buffOffset);
		super.data.put(data, offset, length);
	}

	public void put(int index, byte f) {
		super.data.limit(super.data.capacity());
		super.data.put(index, f);
	}

	@Override
	protected ByteBuffer genBuffer(int size) {
		return BufferUtils.createByteBuffer(size);
	}

	@Override
	protected void glGetBufferSubData(int target, long offset, ByteBuffer data) {
		GL15.glGetBufferSubData(target, offset, data);
	}

	@Override
	protected void glBufferSubData(int target, long offset, ByteBuffer data) {
		GL15.glBufferSubData(target, offset, data);
	}
}
