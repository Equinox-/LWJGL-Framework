package com.pi.core.misc;

import org.lwjgl.opengl.GL30;

import com.pi.core.debug.FrameCounter;
import com.pi.core.debug.FrameCounter.FrameParam;
import com.pi.core.util.Bindable;
import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GLRef;
import com.pi.core.util.GPUObject;
import com.pi.util.ReferenceTable;

public class VertexArrayObject extends GPUObject<VertexArrayObject> implements GLIdentifiable, Bindable {
	private static final ReferenceTable<VertexArrayObject> bound = new ReferenceTable<>(1);

	private int glref = GLRef.NULL;

	public static void unbind() {
		if (bound.isEmpty(0))
			return;
		GL30.glBindVertexArray(0);
		FrameCounter.increment(FrameParam.VAO_CHANGE);
		bound.empty(0);
	}

	@Override
	public void bind() {
		if (GLRef.isNull(glref))
			throw new IllegalStateException("Can't bind an unallocated VAO");
		if (bound.isAttached(0, this))
			return;
		GL30.glBindVertexArray(glref);
		bound.attach(0, this);
		FrameCounter.increment(FrameParam.VAO_CHANGE);
	}

	@Override
	public int getID() {
		return glref;
	}

	@Override
	protected void gpuAllocInternal() {
		glref = GL30.glGenVertexArrays();
		if (GLRef.isNull(glref))
			throw new NullPointerException("Unable to create a VAO");
	}

	@Override
	protected void gpuFreeInternal() {
		GL30.glDeleteVertexArrays(glref);
		glref = GLRef.NULL;
	}
}
