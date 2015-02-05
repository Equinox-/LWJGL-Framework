package com.pi.gl.mesh;

public @interface AttrLayout {
	public int layout();

	public int dimension() default -1;
}
