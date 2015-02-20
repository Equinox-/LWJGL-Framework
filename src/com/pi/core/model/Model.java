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
	private final int indexType, indexCount;
	private final PrimitiveType mode;

	private final GLGenericBuffer wireframeBuffer;
	private final int lineCount;

	private static int chooseIndexSize(int maxVertexID) {
		if (maxVertexID < (1 << 8)) {
			return 1;
		} else if (maxVertexID < (1 << 16)) {
			return 2;
		} else {
			return 4;
		}
	}

	private void insertWireframeLine(int lineNum, int a, int b) {
		switch (indexType) {
		case GL11.GL_UNSIGNED_BYTE:
			wireframeBuffer.put(lineNum << 1, (byte) a);
			wireframeBuffer.put((lineNum << 1) + 1, (byte) b);
			break;
		case GL11.GL_UNSIGNED_SHORT:
			ShortBuffer sbuff = wireframeBuffer.shortImageAt(0);
			sbuff.put(lineNum << 1, (short) a);
			sbuff.put((lineNum << 1) + 1, (short) b);
			break;
		case GL11.GL_UNSIGNED_INT:
			IntBuffer ibuff = wireframeBuffer.integerImageAt(0);
			ibuff.put(lineNum << 1, a);
			ibuff.put((lineNum << 1) + 1, b);
			break;
		}
	}

	public Model(VertexData<E> vertexData, int[] indices, PrimitiveType mode) {
		if ((mode == PrimitiveType.TRIANGLES
				|| mode == PrimitiveType.TRIANGLE_FAN || mode == PrimitiveType.TRIANGLE_STRIP)
				&& indices.length < 3)
			throw new IllegalArgumentException(
					"Can't make a triangle type model with less than three indices");
		if ((mode == PrimitiveType.LINES || mode == PrimitiveType.LINE_STRIP || mode == PrimitiveType.LINE_LOOP)
				&& indices.length < 2)
			throw new IllegalArgumentException(
					"Can't make a line type model with less than two indices");

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
		case 4:
			indexType = GL11.GL_UNSIGNED_INT;
			IntBuffer ibuffer = indexBuffer.integerImageAt(0);
			for (int i = 0; i < indices.length; i++)
				ibuffer.put(i, indices[i]);
			break;
		default:
			throw new RuntimeException("Invalid index size."); // Should never, ever, ever happen
		}

		int l = 0;
		switch (mode) {
		case TRIANGLES:
			lineCount = indices.length;
			wireframeBuffer = new GLGenericBuffer(indexSize * lineCount * 2);
			l = 0;
			for (int i = 2; i < indices.length; i += 3) {
				insertWireframeLine(l++, indices[i], indices[i - 1]);
				insertWireframeLine(l++, indices[i - 1], indices[i - 2]);
				insertWireframeLine(l++, indices[i - 2], indices[i]);
			}
			break;
		case TRIANGLE_STRIP:
		case TRIANGLE_FAN:
			lineCount = 1 + (indices.length - 2) << 1;
			wireframeBuffer = new GLGenericBuffer(indexSize * lineCount * 2);
			l = 0;
			insertWireframeLine(l++, indices[0], indices[1]);
			for (int i = 2; i < indices.length; i++) {
				insertWireframeLine(l++, indices[i - 1], indices[i]);
				insertWireframeLine(l++,
						mode == PrimitiveType.TRIANGLE_FAN ? indices[0]
								: indices[i - 2], indices[i]);
			}
			break;
		default:
			lineCount = 0;
			wireframeBuffer = null;
			break;
		}
	}

	@Override
	public void gpuAlloc() {
		indexUploaded = false;
		vertexData.gpuAlloc();
		indexBuffer.gpuAlloc();
		if (wireframeBuffer != null)
			wireframeBuffer.gpuAlloc();
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
		if (!indexUploaded) { // The index can't change, therefore only need to upload once per alloc. TODO Watch this
			indexBuffer.gpuUpload();
			if (wireframeBuffer != null)
				wireframeBuffer.gpuUpload();
		}
		indexUploaded = true;

		vertexData.gpuUpload();
	}

	public void render() {
		vertexData.activate();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getID());
		GL11.glDrawElements(mode.mode(), indexCount, indexType, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		vertexData.deactive();
	}

	public void renderWireframe() {
		if (wireframeBuffer == null) {
			render(); // Line based info so just render normally
		} else {
			vertexData.activate();
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER,
					wireframeBuffer.getID());
			GL11.glDrawElements(GL11.GL_LINES, lineCount * 2, indexType, 0);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
			vertexData.deactive();
		}
	}
}
