package com.pi.math;

import java.nio.FloatBuffer;

public class Vector3 {
	public float x, y, z;

	public Vector3() {
		this(0, 0, 0);
	}

	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3(FloatBuffer f, int i) {
		this.x = f.get(i);
		this.y = f.get(i + 1);
		this.z = f.get(i + 2);
	}

	public void write(FloatBuffer f, int i) {
		f.put(i, x);
		f.put(i + 1, y);
		f.put(i + 2, z);
	}

	public static Vector3 add(final Vector3 a, final Vector3 b) {
		return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public static Vector3 multiply(final Vector3 v, final float f) {
		return new Vector3(v.x * f, v.y * f, v.z * f);
	}

	public static Vector3 lincom(final Vector3 a, final float aV,
			final Vector3 b, final float bV) {
		Vector3 v = new Vector3();
		v.x = a.x * aV + b.x * bV;
		v.y = a.y * aV + b.y * bV;
		v.z = a.z * aV + b.z * bV;
		return v;
	}

	public static void addto(Vector3 a, final Vector3 b, final float bV) {
		a.x += b.x * bV;
		a.y += b.y * bV;
		a.z += b.z * bV;
	}

	public static Vector3 lincom(final Vector3 a, final float aV,
			final Vector3 b, final float bV, final Vector3 c, final float cV) {
		Vector3 v = new Vector3();
		v.x = a.x * aV + b.x * bV + c.x * cV;
		v.y = a.y * aV + b.y * bV + c.y * cV;
		v.z = a.z * aV + b.z * bV + c.z * cV;
		return v;
	}

	public static float mag2(final Vector3 v) {
		return (v.x * v.x + v.y * v.y + v.z * v.z);
	}

	public static float mag(final Vector3 v) {
		return (float) Math.sqrt(mag2(v));
	}

	public static Vector3 normalize(Vector3 o) {
		Vector3 v = new Vector3(o.x, o.y, o.z);
		float outMag = mag(v);
		v.x /= outMag;
		v.y /= outMag;
		v.z /= outMag;
		return v;
	}

	public static Vector3 cross(final Vector3 a, final Vector3 b) {
		Vector3 res = new Vector3();
		res.x = a.y * b.z - a.z * b.y;
		res.y = a.z * b.x - a.x * b.z;
		res.z = a.x * b.y - a.y * b.x;
		return res;
	}

	public static float dot(final Vector3 a, final Vector3 b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public static float tris_area(final Vector3 a, final Vector3 b,
			final Vector3 c) {
		return mag(cross(lincom(a, 1, b, -1), lincom(c, 1, b, -1))) / 2.0f;
	}

	public String toString() {
		return x + "," + y + "," + z;
	}
}
