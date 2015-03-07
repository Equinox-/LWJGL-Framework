package com.pi.core.model;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL40;

import com.pi.core.buffers.GLGenericBuffer;
import com.pi.core.util.GPUObject;

public class IndexBuffer extends GPUObject<IndexBuffer> {
	public final GLGenericBuffer indexBuffer;
	public final int indexType, indexSize, indexCount;
	public final int indexStride;
	public final PrimitiveType mode;

	private static int chooseIndexSize(int maxVertexID) {
		if (maxVertexID < (1 << 8)) {
			return 1;
		} else if (maxVertexID < (1 << 16)) {
			return 2;
		} else {
			return 4;
		}
	}

	public IndexBuffer(PrimitiveType mode, int[] indices) {
		this(mode, indices, 0, indices.length);
	}

	public IndexBuffer(PrimitiveType mode, GLGenericBuffer indices,
			int indexSize) {
		this.indexCount = indices.size() / indexSize;
		this.mode = mode;

		if ((mode == PrimitiveType.TRIANGLES
				|| mode == PrimitiveType.TRIANGLE_FAN
				|| mode == PrimitiveType.TRIANGLE_STRIP || mode == PrimitiveType.TRIANGLE_PATCHES)
				&& indexCount < 3)
			throw new IllegalArgumentException(
					"Can't make a triangle type index buffer with less than three indices");
		if ((mode == PrimitiveType.LINES || mode == PrimitiveType.LINE_STRIP || mode == PrimitiveType.LINE_LOOP)
				&& indexCount < 2)
			throw new IllegalArgumentException(
					"Can't make a line type index buffer with less than two indices");

		this.indexSize = indexSize;
		this.indexBuffer = indices;
		this.indexStride = -1;
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

	public IndexBuffer(PrimitiveType mode, int[] indices, int offset, int count) {
		if ((mode == PrimitiveType.TRIANGLES
				|| mode == PrimitiveType.TRIANGLE_FAN
				|| mode == PrimitiveType.TRIANGLE_STRIP || mode == PrimitiveType.TRIANGLE_PATCHES)
				&& count < 3)
			throw new IllegalArgumentException(
					"Can't make a triangle type index buffer with less than three indices");
		if ((mode == PrimitiveType.LINES || mode == PrimitiveType.LINE_STRIP || mode == PrimitiveType.LINE_LOOP)
				&& count < 2)
			throw new IllegalArgumentException(
					"Can't make a line type index buffer with less than two indices");

		final int rightOffset = count + offset;

		int maxIndex = 0;
		for (int i = offset; i < rightOffset; i++)
			maxIndex = Math.max(indices[i], maxIndex);

		this.mode = mode;
		this.indexCount = count;
		this.indexSize = chooseIndexSize(maxIndex);
		this.indexBuffer = new GLGenericBuffer(indexSize * indexCount);

		// If we are patches we have to limit the traversal of the index buffer to GL_MAX_PATCH_VERTICES.
		if (mode == PrimitiveType.TRIANGLE_PATCHES) {
			int trisPerPatch = GL11.glGetInteger(GL40.GL_MAX_PATCH_VERTICES) / 3;
			this.indexStride = trisPerPatch * 3;
		} else {
			this.indexStride = -1;
		}

		switch (indexSize) {
		case 1:
			indexType = GL11.GL_UNSIGNED_BYTE;
			for (int i = offset; i < rightOffset; i++)
				indexBuffer.put(i - offset, (byte) indices[i]);
			break;
		case 2:
			indexType = GL11.GL_UNSIGNED_SHORT;
			ShortBuffer sbuffer = indexBuffer.shortImageAt(0);
			for (int i = offset; i < rightOffset; i++)
				sbuffer.put(i - offset, (short) indices[i]);
			break;
		case 4:
			indexType = GL11.GL_UNSIGNED_INT;
			IntBuffer ibuffer = indexBuffer.integerImageAt(0);
			for (int i = offset; i < rightOffset; i++)
				ibuffer.put(i - offset, indices[i]);
			break;
		default:
			throw new RuntimeException("Invalid index size."); // Should never, ever, ever happen
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
		if (indexStride > 0 && indexStride < indexCount) {
			for (int i = 0; i < indexCount; i += indexStride) {
				GL11.glDrawElements(mode.mode(),
						Math.min(indexCount - i, indexStride), indexType,
						indexSize * i);
			}
		} else {
			GL11.glDrawElements(mode.mode(), indexCount, indexType, 0);
		}
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@Override
	protected IndexBuffer me() {
		return this;
	}
}
