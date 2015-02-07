package com.pi.core.vertex;

import com.pi.math.vector.VectorBuff;

public class VertexTypes {
	private VertexTypes() {
	}

	public static class NDCVertex2D {
		@AttrLayout(layout = 0, dimension = 2)
		public VectorBuff pos;
	}

	public static class ColoredVertex3D {
		@AttrLayout(layout = 0, dimension = 3)
		public VectorBuff pos;
		@AttrLayout(layout = 1, dimension = 3)
		public VectorBuff normal;
		@AttrLayout(layout = 2)
		public BufferColor color;
	}

	public static class TexturedVertex3D {
		@AttrLayout(layout = 0, dimension = 3)
		public VectorBuff pos;
		@AttrLayout(layout = 1, dimension = 3)
		public VectorBuff normal;
		@AttrLayout(layout = 2, dimension = 2)
		public VectorBuff texture;
	}
}
