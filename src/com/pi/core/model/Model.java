package com.pi.core.model;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.pi.core.buffers.GLGenericBuffer;
import com.pi.core.util.GPUObject;
import com.pi.core.vertex.VertexData;

public class Model<E> implements GPUObject {
	private final VertexData<E> vertexData;
	private final GLGenericBuffer indexBuffer;
	private final int indexType, mode, indexCount;

	private static int chooseIndexSize(int maxVertexID) {
		if (maxVertexID < (1 << 8)) {
			return 1;
		} else if (maxVertexID < (1 << 16)) {
			return 2;
		} else {
			return 4;
		}
	}

	public Model(VertexData<E> vertexData, int[] indices, int mode) {
		this.mode = mode;
		this.indexCount = indices.length;
		this.vertexData = vertexData;
		int indexSize = chooseIndexSize(vertexData.vertexDB.length);
		this.indexBuffer = new GLGenericBuffer(indexSize * indices.length);
		switch (indexSize) {
		case 1:
			indexType = GL11.GL_UNSIGNED_BYTE;
			for (int i = 0; i < indices.length; i++)
				indexBuffer.put(i, (byte) indices[i]);
			break;
		case 2:
			indexType = GL11.GL_UNSIGNED_SHORT;
			ShortBuffer sbuffer = indexBuffer.shortImageAt(0);
			for (int i = 0; i < indices.length; i++)
				sbuffer.put(i, (short) indices[i]);
			break;
		case 3:
			indexType = GL11.GL_UNSIGNED_INT;
			IntBuffer ibuffer = indexBuffer.integerImageAt(0);
			for (int i = 0; i < indices.length; i++)
				ibuffer.put(i, indices[i]);
			break;
		default:
			throw new RuntimeException("Invalid index size."); // Should never, ever, ever happen
		}
	}

	@Override
	public void gpuAlloc() {
		indexUploaded = false;
		vertexData.gpuAlloc();
		indexBuffer.gpuAlloc();
	}

	@Override
	public void gpuFree() {
		indexUploaded = false;
		indexBuffer.gpuFree();
		vertexData.gpuFree();
	}

	private boolean indexUploaded = false;

	@Override
	public void gpuUpload() {
		if (!indexUploaded) // The index can't change, therefore only need to upload once per alloc. TODO Watch this
			indexBuffer.gpuUpload();
		indexUploaded = true;

		vertexData.gpuUpload();
	}

	public void render() {
		vertexData.activate();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getID());
		GL11.glDrawElements(mode, indexCount, indexType, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		vertexData.deactive();
	}
}
