package com.pi.user.camera;

import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.VectorBuff3;

public interface Camera {
	public VectorBuff3 position(VectorBuff3 dest);

	public void update();

	public Matrix4 view();
}
