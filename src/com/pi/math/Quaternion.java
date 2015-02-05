package com.pi.math;

public class Quaternion {
	public float w;
	public Vector3 v;

	public static Quaternion multiply(final Quaternion a, final Quaternion b) {
		Quaternion res = new Quaternion();
		res.w = a.w * b.w - Vector3.dot(a.v, b.v);
		res.v = Vector3.lincom(a.v, b.w, b.v, a.w, Vector3.cross(a.v, b.v), 1);
		return res;
	}

	public static Quaternion multiply(final Quaternion a, final Vector3 b) {
		Quaternion res = new Quaternion();
		res.w = -Vector3.dot(a.v, b);
		res.v = Vector3.lincom(b, a.w, Vector3.cross(a.v, b), 1);
		return res;
	}

	public static float mag2(final Quaternion a) {
		return a.w * a.w + a.v.x * a.v.x + a.v.y * a.v.y + a.v.z * a.v.z;
	}

	public static float mag(final Quaternion a) {
		return (float) Math.sqrt(mag2(a));
	}

	public static void normalizeHere(Quaternion a) {
		float mag = 1.0f / mag(a);
		a.w *= mag;
		a.v.x *= mag;
		a.v.y *= mag;
		a.v.z *= mag;
	}

	public static void addto(Quaternion a, final Quaternion b, final float f) {
		a.w += b.w * f;
		a.v.x += b.v.x * f;
		a.v.y += b.v.y * f;
		a.v.z += b.v.z * f;
	}
}
