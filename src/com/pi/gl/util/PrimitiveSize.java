package com.pi.gl.util;

public class PrimitiveSize {
	public static int sizeof(Class<?> t) {
		if (!t.isPrimitive() && !Number.class.isAssignableFrom(t)
				&& t != Boolean.class)
			throw new IllegalArgumentException(
					"Only primitives are supported by sizeof()");
		if (t == byte.class || t == Byte.class || t == boolean.class
				|| t == Boolean.class) {
			return 1;
		} else if (t == short.class || t == Short.class) {
			return 2;
		} else if (t == int.class || t == Integer.class || t == float.class
				|| t == Float.class) {
			return 4;
		} else if (t == long.class || t == Long.class || t == double.class
				|| t == Double.class) {
			return 8;
		} else {
			throw new IllegalArgumentException("Primitive " + t
					+ " is not recognized.");
		}
	}
}
