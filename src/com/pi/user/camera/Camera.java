package com.pi.user.camera;

import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.VectorBuff3;

public interface Camera {
	public Matrix4 apply(Matrix4 matrix);

	public void update();

	public VectorBuff3 position(VectorBuff3 dest);
}
