package com.pi.gl.vertex;

import com.pi.math.vector.VectorBuff;

public class ColoredVertex {
	@AttrLayout(layout = 0, dimension = 2)
	public VectorBuff pos;
	@AttrLayout(layout = 1, dimension = 3)
	public VectorBuff normal;
	@AttrLayout(layout = 2)
	public BufferColor color;
}
