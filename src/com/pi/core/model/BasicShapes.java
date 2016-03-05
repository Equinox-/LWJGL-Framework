package com.pi.core.model;

import java.util.function.Supplier;

import com.pi.core.vertex.VertexData;
import com.pi.core.vertex.VertexTypes.Vertex2D;

public class BasicShapes {
	private static ThreadLocal<BasicShapes> basic = ThreadLocal
			.withInitial(new Supplier<BasicShapes>() {
				@Override
				public BasicShapes get() {
					return new BasicShapes();
				}
			});

	public static void removeShapes() {
		shapes().gpuFree();
		basic.remove();
	}

	public static BasicShapes shapes() {
		return basic.get();
	}

	private Model<Vertex2D> ndcScreenQuad = null;

	public Model<Vertex2D> getNDCScreenQuad() {
		if (ndcScreenQuad == null) {
			VertexData<Vertex2D> plVerts = new VertexData<>(Vertex2D.class, 4);
			plVerts.v(0).pos.setV(-1, -1);
			plVerts.v(1).pos.setV(1, -1);
			plVerts.v(2).pos.setV(1, 1);
			plVerts.v(3).pos.setV(-1, 1);
			ndcScreenQuad = new Model<>(PrimitiveType.TRIANGLES, plVerts,
					new int[] { 0, 1, 2, 0, 2, 3 });
			ndcScreenQuad.gpuAllocInternal();
			ndcScreenQuad.gpuUploadInternal();
			plVerts.cpuFree();
		}
		return ndcScreenQuad;
	}

	public void gpuFree() {
		if (ndcScreenQuad != null)
			ndcScreenQuad.gpuFreeInternal();
	}
}
