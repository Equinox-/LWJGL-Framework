package com.pi.core.vertex;

import com.pi.math.vector.VectorBuff;

public class TexturedVertex {
	@AttrLayout(layout = 0, dimension = 3)
	public VectorBuff pos;
	@AttrLayout(layout = 1, dimension = 3)
	public VectorBuff normal;
	@AttrLayout(layout = 2, dimension = 2)
	public VectorBuff texture;
}
