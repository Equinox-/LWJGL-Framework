package com.pi.util;

public class PrimitiveList {
	public static int[] insert(int[] src, int... values) {
		int[] buffer = src;
		if (buffer == null) {
			buffer = new int[256];
			buffer[0] = 0;
		}
		if (buffer[0] + values.length > buffer.length) {
			// Realloc
			buffer = new int[Math.max(src.length + values.length, src.length * 2)];
			System.arraycopy(src, 0, buffer, 0, src[0] + 1);
		}
		System.arraycopy(values, 0, buffer, 1 + buffer[0], values.length);
		buffer[0] += values.length;
		return buffer;
	}

	public static float[] insert(float[] src, float... values) {
		float[] buffer = src;
		if (buffer == null) {
			buffer = new float[256];
			buffer[0] = 0;
		}
		if (buffer[0] + values.length > buffer.length) {
			// Realloc
			buffer = new float[Math.max(src.length + values.length, src.length * 2)];
			System.arraycopy(src, 0, buffer, 0, (int) src[0] + 1);
		}
		System.arraycopy(values, 0, buffer, 1 + (int) buffer[0], values.length);
		buffer[0] += values.length;
		return buffer;
	}
}
