package com.pi.core.vertex;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.pi.core.buffers.BufferAccessHint;
import com.pi.core.buffers.BufferModifyHint;
import com.pi.core.buffers.GLGenericBuffer;
import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GPUObject;
import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.VectorBuff;

public class VertexData<E> implements GPUObject, GLIdentifiable {
	public final E[] vertexDB;
	private final VertexLayout layout;
	private final GLGenericBuffer bufferObject;
	private int vao = -1;

	@SuppressWarnings("unchecked")
	public VertexData(Class<E> vertexClass, int count) {
		this.vertexDB = (E[]) Array.newInstance(vertexClass, count);
		this.layout = new VertexLayout(vertexClass);
		this.layout.validate();
		this.bufferObject = new GLGenericBuffer(layout.byteSize * count);
		link();
	}

	public VertexData<E> access(BufferAccessHint a) {
		this.bufferObject.access(a);
		return this;
	}

	public VertexData<E> modify(BufferModifyHint a) {
		this.bufferObject.modify(a);
		return this;
	}

	private void link() {
		@SuppressWarnings("unchecked")
		Class<E> vertexClass = (Class<E>) vertexDB.getClass()
				.getComponentType();
		// Allocate and link to the GL Generic Buffer.
		for (int i = 0; i < vertexDB.length; i++) {
			try {
				E itm = vertexClass.newInstance();
				int head = i * layout.byteSize;
				for (int j = 0; j < layout.attrMapping.length; j++) {
					Field attr = layout.attrMapping[j];
					if (attr != null) {
						attr.setAccessible(true);
						if (attr.getType().isAssignableFrom(VectorBuff.class)) {
							attr.set(
									itm,
									new VectorBuff(this.bufferObject
											.floatImageAt(head
													+ layout.attrOffset[j]), 0,
											layout.attrSize[j]));
						} else if (attr.getType().isAssignableFrom(
								Matrix4.class)) {
							attr.set(
									itm,
									new Matrix4(this.bufferObject
											.floatImageAt(head
													+ layout.attrOffset[j]), 0));
						} else if (attr.getType().isAssignableFrom(
								BufferColor.class)) {
							attr.set(
									itm,
									new BufferColor(this.bufferObject
											.getBacking(), head
											+ layout.attrOffset[j]));
						}
					}
				}
				vertexDB[i] = itm;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * If you changed the vertex data you need to resync the buffer. This does that.
	 */
	@Override
	public void gpuUpload() {
		bufferObject.gpuUpload();
	}

	@Override
	public void gpuAlloc() {
		if (vao >= 0)
			gpuFree();

		// Dump the buffer
		vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);

		bufferObject.gpuAlloc();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferObject.getID());

		for (int j = 0; j < layout.attrMapping.length; j++) {
			if (layout.attrMapping[j] != null) {
				AttrLayout lay = layout.attrMapping[j]
						.getAnnotation(AttrLayout.class);
				if (layout.attrMapping[j].getType().isAssignableFrom(
						Matrix4.class)) {
					for (int r = 0; r < layout.attrSize[j]; r++)
						GL20.glVertexAttribPointer(lay.layout() + r,
								layout.attrSize[j], layout.attrType[j],
								layout.attrNormalize[j], layout.byteSize,
								layout.attrOffset[j] + r * layout.attrSize[j]
										* 4);
				} else {
					GL20.glVertexAttribPointer(lay.layout(),
							layout.attrSize[j], layout.attrType[j],
							layout.attrNormalize[j], layout.byteSize,
							layout.attrOffset[j]);
				}
			}
		}

		GL30.glBindVertexArray(0);
	}

	@Override
	public void gpuFree() {
		if (vao >= 0)
			GL30.glDeleteVertexArrays(vao);
		bufferObject.gpuFree();
	}

	public void activate() {
		GL30.glBindVertexArray(vao);
		for (int j = 0; j < layout.attrMapping.length; j++) {
			if (layout.attrMapping[j] != null) {
				AttrLayout lay = layout.attrMapping[j]
						.getAnnotation(AttrLayout.class);
				GL20.glEnableVertexAttribArray(lay.layout());
			}
		}
	}

	public void deactive() {
		for (int j = 0; j < layout.attrMapping.length; j++) {
			if (layout.attrMapping[j] != null) {
				AttrLayout lay = layout.attrMapping[j]
						.getAnnotation(AttrLayout.class);
				GL20.glDisableVertexAttribArray(lay.layout());
			}
		}
		GL30.glBindVertexArray(0);
	}

	@Override
	public int getID() {
		return vao;
	}
}
