package com.pi.core.model;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

import com.pi.core.buffers.BufferType;
import com.pi.core.buffers.GLGenericBuffer;
import com.pi.core.util.GPUObject;

public class IndexBuffer extends GPUObject<IndexBuffer> {
	private final GLGenericBuffer indexBuffer;
	private int indexType, indexSize;
	private int indexCount;
	private final PrimitiveType mode;

	private IntBuffer intBuff;
	private ShortBuffer shortBuff;

	public static int chooseIndexSize(int maxVertexID) {
		if (maxVertexID < (1 << 8)) {
			return 1;
		} else if (maxVertexID < (1 << 16)) {
			return 2;
		} else {
			return 4;
		}
	}

	public static int chooseIndexSize(int[] index, int offset, int rightOffset) {
		int maxVertexID = 0;
		for (int i = offset; i < rightOffset; i++)
			maxVertexID = Math.max(index[i], maxVertexID);

		// It's typical for an unsigned byte not to be optimal for the hardware, so don't choose it automatically.
		boolean unsigned_bytes = false;
		if (unsigned_bytes && maxVertexID < (1 << 8)) {
			return 1;
		} else if (maxVertexID < (1 << 16)) {
			return 2;
		} else {
			return 4;
		}
	}

	private static int indexTypeFromSize(int size) {
		switch (size) {
		case 1:
			return GL11.GL_UNSIGNED_BYTE;
		case 2:
			return GL11.GL_UNSIGNED_SHORT;
		case 4:
			return GL11.GL_UNSIGNED_INT;
		default:
			throw new RuntimeException("Invalid index size.");
		}
	}

	private IndexBuffer(PrimitiveType mode, int indexSize, GLGenericBuffer indexBuffer) {
		this.mode = mode;
		indexBuffer.type(BufferType.ELEMENT_ARRAY);
		this.indexBuffer = indexBuffer;
		resize(indexBuffer.size() / indexSize, indexSize);
	}

	public IndexBuffer(PrimitiveType mode, int indexSize, int indexCount) {
		this(mode, indexSize, new GLGenericBuffer(indexSize * indexCount));
	}

	public IndexBuffer(PrimitiveType mode, int[] indices) {
		this(mode, indices, 0, indices.length);
	}

	public IndexBuffer(PrimitiveType mode, GLGenericBuffer indices, int indexSize) {
		this(mode, indexSize, indices);
	}

	public IndexBuffer(PrimitiveType mode, int[] indices, int offset, int count) {
		this(mode, chooseIndexSize(indices, offset, offset + count), count);
		write(indices, offset, count);
	}

	public PrimitiveType mode() {
		return mode;
	}

	public int indexSize() {
		return indexSize;
	}

	public int indexCount() {
		return indexCount;
	}

	public GLGenericBuffer buffer() {
		return indexBuffer;
	}

	public void resize(int nCount, int nSize) {
		if (indexSize * indexCount < nCount * nSize || indexSize * indexCount > (12 + nCount) * nSize) {
			indexBuffer.resize(nCount * nSize);
			this.intBuff = this.indexBuffer.integerImageAt(0);
			this.shortBuff = this.indexBuffer.shortImageAt(0);
		}
		this.indexSize = nSize;
		this.indexCount = nCount;
		this.indexType = indexTypeFromSize(this.indexSize);
	}

	public void write(int[] indices, int offset, int count) {
		this.indexCount = count;
		final int rightOffset = offset + count;
		switch (indexSize) {
		case 1:
			for (int i = offset; i < rightOffset; i++)
				indexBuffer.put(i - offset, (byte) indices[i]);
			break;
		case 2:
			for (int i = offset; i < rightOffset; i++)
				shortBuff.put(i - offset, (short) indices[i]);
			break;
		case 4:
			for (int i = offset; i < rightOffset; i++)
				intBuff.put(i - offset, indices[i]);
			break;
		default:
			// Should never, ever, ever happen
			throw new RuntimeException("Invalid index size.");
		}
	}

	public int getIndex(int k) {
		switch (indexType) {
		case GL11.GL_UNSIGNED_SHORT:
			return shortBuff.get(k) & 0xFFFF;
		case GL11.GL_UNSIGNED_INT:
			return intBuff.get(k);
		case GL11.GL_UNSIGNED_BYTE:
		default:
			return indexBuffer.get(k) & 0xFF;
		}
	}

	@Override
	protected void gpuAllocInternal() {
		indexBuffer.gpuAlloc();
	}

	@Override
	protected void gpuFreeInternal() {
		indexBuffer.gpuFree();
	}

	@Override
	protected void gpuUploadInternal() {
		indexBuffer.gpuUpload();
	}

	public void render() {
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getID());
		GL11.glDrawElements(mode.mode(), indexCount, indexType, 0);
	}

	public void renderInstances(int n) {
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getID());
		GL31.glDrawElementsInstanced(mode.mode(), indexCount, indexType, 0, n);
	}

	@Override
	public String toString() {
		return mode.name() + " x" + (indexCount / mode.stride());
	}
}
