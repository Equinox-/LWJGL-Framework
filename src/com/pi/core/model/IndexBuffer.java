package com.pi.core.model;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.pi.core.buffers.GLGenericBuffer;
import com.pi.core.util.GPUObject;

public class IndexBuffer extends GPUObject<IndexBuffer> {
	public final GLGenericBuffer indexBuffer;
	public final int indexType, indexSize;
	public int indexCount;
	public final PrimitiveType mode;

	private final IntBuffer intBuff;
	private final ShortBuffer shortBuff;

	public static int chooseIndexSize(int[] index, int offset, int rightOffset) {
		int maxVertexID = 0;
		for (int i = offset; i < rightOffset; i++)
			maxVertexID = Math.max(index[i], maxVertexID);

		if (maxVertexID < (1 << 8)) {
			return 1;
		} else if (maxVertexID < (1 << 16)) {
			return 2;
		} else {
			return 4;
		}
	}

	private IndexBuffer(PrimitiveType mode, int indexSize, int indexCount,
			GLGenericBuffer indexBuffer) {
		this.mode = mode;

//		if ((mode == PrimitiveType.TRIANGLES
//				|| mode == PrimitiveType.TRIANGLE_FAN
//				|| mode == PrimitiveType.TRIANGLE_STRIP || mode == PrimitiveType.TRIANGLE_PATCHES)
//				&& indexCount < 3)
//			throw new IllegalArgumentException(
//					"Can't make a triangle type index buffer with less than three indices");
//		if ((mode == PrimitiveType.LINES || mode == PrimitiveType.LINE_STRIP || mode == PrimitiveType.LINE_LOOP)
//				&& indexCount < 2)
//			throw new IllegalArgumentException(
//					"Can't make a line type index buffer with less than two indices");

		this.indexSize = indexSize;
		this.indexBuffer = indexBuffer;
		this.intBuff = this.indexBuffer.integerImageAt(0);
		this.shortBuff = this.indexBuffer.shortImageAt(0);

		switch (indexSize) {
		case 1:
			indexType = GL11.GL_UNSIGNED_BYTE;
			break;
		case 2:
			indexType = GL11.GL_UNSIGNED_SHORT;
			break;
		case 4:
			indexType = GL11.GL_UNSIGNED_INT;
			break;
		default:
			throw new RuntimeException("Invalid index size."); // Should never, ever, ever happen
		}
	}

	public IndexBuffer(PrimitiveType mode, int indexSize, int indexCount) {
		this(mode, indexSize, indexCount, new GLGenericBuffer(indexSize
				* indexCount));
	}

	public IndexBuffer(PrimitiveType mode, int[] indices) {
		this(mode, indices, 0, indices.length);
	}

	public IndexBuffer(PrimitiveType mode, GLGenericBuffer indices,
			int indexSize) {
		this(mode, indexSize, indices.size() / indexSize, indices);
	}

	public IndexBuffer(PrimitiveType mode, int[] indices, int offset, int count) {
		this(mode, chooseIndexSize(indices, offset, offset + count), count);
		write(indices, offset, count);
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
			ShortBuffer sbuffer = indexBuffer.shortImageAt(0);
			for (int i = offset; i < rightOffset; i++)
				sbuffer.put(i - offset, (short) indices[i]);
			break;
		case 4:
			IntBuffer ibuffer = indexBuffer.integerImageAt(0);
			for (int i = offset; i < rightOffset; i++)
				ibuffer.put(i - offset, indices[i]);
			break;
		default:
			throw new RuntimeException("Invalid index size."); // Should never, ever, ever happen
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
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@Override
	protected IndexBuffer me() {
		return this;
	}
}
