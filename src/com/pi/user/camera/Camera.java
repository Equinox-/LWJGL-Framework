package com.pi.user.camera;

import com.pi.math.matrix.Matrix4;

public interface Camera {
	public Matrix4 apply(Matrix4 matrix);

	public void update();
}
