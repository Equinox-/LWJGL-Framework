package com.pi.core.model;

import com.pi.core.util.GPUObject;
import com.pi.core.vertex.VertexData;

public class Model<E> implements GPUObject {
	private final VertexData<E> vertexData;
	private final IndexBuffer[] indexes;

	public Model(PrimitiveType mode, VertexData<E> vertexData, int[]... index) {
		if (index.length < 1)
			throw new IllegalArgumentException(
					"Must include at least one index buffer.");
		this.vertexData = vertexData;
		this.indexes = new IndexBuffer[index.length];
		for (int i = 0; i < index.length; i++)
			this.indexes[i] = new IndexBuffer(mode, index[i]);
	}

	@Override
	public void gpuAlloc() {
		indexUploaded = false;
		vertexData.gpuAlloc();
		for (IndexBuffer index : indexes)
			index.gpuAlloc();
	}

	@Override
	public void gpuFree() {
		indexUploaded = false;
		for (IndexBuffer index : indexes)
			index.gpuFree();
		vertexData.gpuFree();
	}

	private boolean indexUploaded = false;

	@Override
	public void gpuUpload() {
		if (!indexUploaded) { // The index can't change, therefore only need to upload once per alloc. TODO Watch this
			for (IndexBuffer index : indexes)
				index.gpuUpload();
		}
		indexUploaded = true;

		vertexData.gpuUpload();
	}

	public void render() {
		render(0);
	}

	public void render(int indexID) {
		vertexData.activate();
		indexes[indexID].render();
		vertexData.deactive();
	}
}
