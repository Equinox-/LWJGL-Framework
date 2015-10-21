package com.pi.core.model;

import java.util.Arrays;

import com.pi.core.util.GPUObject;
import com.pi.core.vertex.VertexData;

public class Model<E> extends GPUObject<Model<E>> {
	public final VertexData<E> vertexData;
	public final IndexBuffer[] indexes;

	public Model(PrimitiveType mode, VertexData<E> vertexData, int[]... index) {
		if (index.length < 1)
			throw new IllegalArgumentException(
					"Must include at least one index buffer.");
		this.vertexData = vertexData;
		this.indexes = new IndexBuffer[index.length];
		for (int i = 0; i < index.length; i++)
			this.indexes[i] = new IndexBuffer(mode, index[i]);
	}

	public Model(VertexData<E> vertexData, IndexBuffer... index) {
		if (index.length < 1)
			throw new IllegalArgumentException(
					"Must include at least one index buffer.");
		this.vertexData = vertexData;
		this.indexes = index;
	}

	public final E vtx(int id) {
		return vertexData.v(id);
	}

	@Override
	protected void gpuAllocInternal() {
		indexUploaded = false;
		vertexData.gpuAlloc();
		for (IndexBuffer index : indexes)
			if (index != null)
				index.gpuAlloc();
	}

	@Override
	protected void gpuFreeInternal() {
		indexUploaded = false;
		for (IndexBuffer index : indexes)
			if (index != null)
				index.gpuFree();
		vertexData.gpuFree();
	}

	private boolean indexUploaded = false;

	@Override
	protected void gpuUploadInternal() {
		if (!indexUploaded) { // The index can't change, therefore only need to
								// upload once per alloc. TODO Watch this
			for (IndexBuffer index : indexes)
				index.gpuUploadInternal();
		}
		indexUploaded = true;

		vertexData.gpuUpload();
	}

	public void render() {
		vertexData.activate();
		indexes[0].render();
	}

	public void render(int... indexBuffers) {
		vertexData.activate();
		for (int indexID : indexBuffers)
			indexes[indexID].render();
		// VertexData.deactivate(); Don't need this in theory.
	}


	@Override
	public String toString() {
		return "Model[" + vertexData + ", " + Arrays.toString(indexes) + "]";
	}
}
