package com.pi.core.vertex;

import com.pi.math.vector.VectorBuff2;
import com.pi.math.vector.VectorBuff3;

public class VertexTypes {
	private VertexTypes() {
	}

	public static class Vertex2D {
		@AttrLayout(layout = 0, dimension = 2)
		public VectorBuff2 pos;
	}

	public static class Vertex3D {
		@AttrLayout(layout = 0, dimension = 3)
		public VectorBuff3 pos;
	}

	public static class LitVertex3D extends Vertex3D {
		@AttrLayout(layout = 1, dimension = 3)
		public VectorBuff3 normal;
	}

	public static class ColoredVertex3D extends LitVertex3D {
		@AttrLayout(layout = 2)
		public BufferColor color;
	}

	public static class TexturedVertex3D extends LitVertex3D {
		@AttrLayout(layout = 2, dimension = 2)
		public VectorBuff2 texture;
	}
}
