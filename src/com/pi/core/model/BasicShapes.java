package com.pi.core.model;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL;

import com.pi.core.vertex.VertexData;
import com.pi.core.vertex.VertexTypes.Vertex2D;

public class BasicShapes {
	private static Map<Long, BasicShapes> basic = new HashMap<>();

	public static BasicShapes shapes() {
		long ctx = GL.getCurrent().getPointer();
		BasicShapes res = basic.get(ctx);
		if (res == null)
			basic.put(ctx, res = new BasicShapes());
		return res;
	}

	public static void removeShapes() {
		shapes().gpuFree();
		basic.remove(GL.getCurrent().getPointer());
	}

	private Model<Vertex2D> ndcScreenQuad = null;

	public Model<Vertex2D> getNDCScreenQuad() {
		if (ndcScreenQuad == null) {
			VertexData<Vertex2D> plVerts = new VertexData<>(
					Vertex2D.class, 4);
			plVerts.vertexDB[0].pos.setV(-1, -1);
			plVerts.vertexDB[1].pos.setV(1, -1);
			plVerts.vertexDB[2].pos.setV(1, 1);
			plVerts.vertexDB[3].pos.setV(-1, 1);
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
