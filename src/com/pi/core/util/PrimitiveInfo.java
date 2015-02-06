package com.pi.core.util;

import org.lwjgl.opengl.GL11;

public class PrimitiveInfo {
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

	public static int glType(Class<?> t) {
		if (!t.isPrimitive() && !Number.class.isAssignableFrom(t)
				&& t != Boolean.class)
			throw new IllegalArgumentException(
					"Only primitives are supported by glType()");
		if (t == byte.class || t == Byte.class || t == boolean.class
				|| t == Boolean.class) {
			return GL11.GL_UNSIGNED_BYTE;
		} else if (t == short.class || t == Short.class) {
			return GL11.GL_SHORT;
		} else if (t == int.class || t == Integer.class) {
			return GL11.GL_INT;
		} else if (t == float.class || t == Float.class) {
			return GL11.GL_FLOAT;
		} else if (t == long.class || t == Long.class) {
			throw new RuntimeException(
					"OpenGL typically doesn't support 64bit integers");
		} else if (t == double.class || t == Double.class) {
			return GL11.GL_DOUBLE;
		} else {
			throw new IllegalArgumentException("Primitive " + t
					+ " is not recognized.");
		}
	}
}
