package com.pi.core.vertex;

import java.awt.Color;
import java.nio.ByteBuffer;

public final class BufferColor {
	private final ByteBuffer backer;
	private final int offset;

	public BufferColor(ByteBuffer backer, int offset) {
		this.backer = backer;
		this.offset = offset;
	}

	public BufferColor set(int r, int g, int b, int a) {
		backer.put(offset, (byte) r);
		backer.put(offset + 1, (byte) g);
		backer.put(offset + 2, (byte) b);
		backer.put(offset + 3, (byte) a);
		return this;
	}

	public BufferColor setAlpha(float a) {
		backer.put(offset + 3, (byte) (a * 0xFF));
		return this;
	}

	public BufferColor set(float r, float g, float b, float a) {
		return set((int) (r * 255), (int) (g * 255), (int) (b * 255),
				(int) (a * 255));
	}

	public BufferColor set(int r, int g, int b) {
		return set(r, g, b, 255);
	}

	public BufferColor set(float r, float g, float b) {
		return set(r, g, b, 1);
	}

	public BufferColor set(int argb) {
		return set((argb >> 24) & 0xFF, (argb >> 16) & 0xFF,
				(argb >> 8) & 0xFF, argb & 0xFF);
	}

	public BufferColor set(Color src) {
		return set(src.getRed(), src.getGreen(), src.getBlue(), src.getAlpha());
	}

	public BufferColor set(BufferColor c) {
		return set(c.backer.get(c.offset), c.backer.get(c.offset + 1),
				c.backer.get(c.offset + 2), c.backer.get(c.offset + 3));
	}

	public ByteBuffer getAccessor() {
		int po = backer.position();
		int lim = backer.limit();
		backer.position(offset);
		backer.limit(offset + 4);
		ByteBuffer buf = backer.slice();
		backer.limit(lim);
		backer.position(po);
		return buf;
	}

	@Override
	public String toString() {
		return (backer.get(offset) & 0xFF) + ","
				+ (backer.get(offset + 1) & 0xFF) + ","
				+ (backer.get(offset + 2) & 0xFF) + ","
				+ (backer.get(offset + 3) & 0xFF);
	}

	public boolean isColor(int r, int g, int b, int a) {
		return (backer.get(offset) & 0xFF) == r
				&& (backer.get(offset + 1) & 0xFF) == g
				&& (backer.get(offset + 2) & 0xFF) == b
				&& (backer.get(offset + 3) & 0xFF) == a;
	}

	public boolean isColor(BufferColor o) {
		return (backer.get(offset) & 0xFF) == (o.backer.get(offset) & 0xFF)
				&& (backer.get(offset + 1) & 0xFF) == (o.backer.get(offset + 1) & 0xFF)
				&& (backer.get(offset + 2) & 0xFF) == (o.backer.get(offset + 2) & 0xFF)
				&& (backer.get(offset + 3) & 0xFF) == (o.backer.get(offset + 3) & 0xFF);
	}
}
