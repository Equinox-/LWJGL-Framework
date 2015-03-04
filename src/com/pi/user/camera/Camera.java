package com.pi.user.camera;

import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.Vector;

public interface Camera {
	public Matrix4 apply(Matrix4 matrix);

	public void update();

	public Vector position();
	
	public void transformRay(Vector origin, Vector direction);
}
