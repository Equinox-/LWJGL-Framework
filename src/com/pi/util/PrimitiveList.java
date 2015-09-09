package com.pi.util;

public class PrimitiveList {
	public static int[] insert(int[] buffer, int... values) {
		if (buffer == null) {
			buffer = new int[256];
			buffer[0] = 0;
		}
		if (buffer[0] + values.length > buffer.length) {
			// Realloc
			int[] tmp = buffer;
			buffer = new int[Math.max(tmp.length + values.length,
					tmp.length * 2)];
			System.arraycopy(tmp, 0, buffer, 0, tmp[0] + 1);
		}
		System.arraycopy(values, 0, buffer, 1 + buffer[0], values.length);
		buffer[0] += values.length;
		return buffer;
	}

	public static float[] insert(float[] buffer, float... values) {
		if (buffer == null) {
			buffer = new float[256];
			buffer[0] = 0;
		}
		if (buffer[0] + values.length > buffer.length) {
			// Realloc
			float[] tmp = buffer;
			buffer = new float[Math.max(tmp.length + values.length,
					tmp.length * 2)];
			System.arraycopy(tmp, 0, buffer, 0, (int) tmp[0] + 1);
		}
		System.arraycopy(values, 0, buffer, 1 + (int) buffer[0], values.length);
		buffer[0] += values.length;
		return buffer;
	}
}
