package com.pi.core.misc;

import java.lang.ref.WeakReference;

import org.lwjgl.opengl.GL30;

import com.pi.core.util.Bindable;
import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GPUObject;

public class VertexArrayObject extends GPUObject<VertexArrayObject>implements GLIdentifiable, Bindable {
	private static WeakReference<VertexArrayObject> current = null;

	private int vao = -1;

	@Override
	public int getID() {
		return vao;
	}

	@Override
	protected void gpuAllocInternal() {
		vao = GL30.glGenVertexArrays();
	}

	@Override
	protected void gpuFreeInternal() {
		GL30.glDeleteVertexArrays(vao);
	}

	@Override
	public void bind() {
		if (current != null && current.get() == this)
			return;
		GL30.glBindVertexArray(vao);
		current = new WeakReference<>(this);
	}

	public static void unbind() {
		if (current == null)
			return;
		GL30.glBindVertexArray(0);
		current = null;
	}
}