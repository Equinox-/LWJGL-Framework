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
	public E[] vertexDB;
	private final Class<E> vertexClass;
	private final int count;
	private final VertexLayout layout;
	private final GLGenericBuffer bufferObject;
	private int vao = -1;

	public VertexData(Class<E> vertexClass, int count) {
		this.vertexClass = vertexClass;
		this.count = count;
		this.layout = new VertexLayout(vertexClass);
		this.layout.validate();
		this.bufferObject = new GLGenericBuffer(layout.structureSize * count);

		cpuAlloc();
	}

	public VertexData<E> access(BufferAccessHint a) {
		this.bufferObject.access(a);
		return this;
	}

	public VertexData<E> modify(BufferModifyHint a) {
		this.bufferObject.modify(a);
		return this;
	}

	@SuppressWarnings("unchecked")
	public void cpuAlloc() {
		this.bufferObject.cpuAlloc();
		this.vertexDB = (E[]) Array.newInstance(vertexClass, count);
		// Allocate and link to the GL Generic Buffer.
		for (int i = 0; i < vertexDB.length; i++) {
			try {
				E itm = vertexClass.newInstance();
				int head = i * layout.structureSize;
				for (int j = 0; j < layout.attrMapping.length; j++) {
					Field attr = layout.attrMapping[j];
					if (attr != null) {
						attr.setAccessible(true);
						if (attr.getType().isArray()) {
							Class<?> type = attr.getType().getComponentType();
							Object array = attr.get(itm);
							if (array == null) {
								array = Array.newInstance(type, attr
										.getAnnotation(AttrLayout.class)
										.arraySize());
								attr.set(itm, array);
							}

							if (type.isAssignableFrom(VectorBuff.class)) {
								Array.set(
										array,
										layout.attrIndex[j],
										new VectorBuff(
												this.bufferObject
														.floatImageAt(head
																+ layout.attrOffset[j]),
												0, layout.attrSize[j]));
							} else if (type.isAssignableFrom(Matrix4.class)) {
								Array.set(
										array,
										layout.attrIndex[j],
										new Matrix4(
												this.bufferObject
														.floatImageAt(head
																+ layout.attrOffset[j]),
												0));
							} else if (type.isAssignableFrom(BufferColor.class)) {
								Array.set(
										array,
										layout.attrIndex[j],
										new BufferColor(this.bufferObject
												.getBacking(), head
												+ layout.attrOffset[j]));
							}
						} else {
							if (attr.getType().isAssignableFrom(
									VectorBuff.class)) {
								attr.set(
										itm,
										new VectorBuff(
												this.bufferObject
														.floatImageAt(head
																+ layout.attrOffset[j]),
												0, layout.attrSize[j]));
							} else if (attr.getType().isAssignableFrom(
									Matrix4.class)) {
								attr.set(
										itm,
										new Matrix4(
												this.bufferObject
														.floatImageAt(head
																+ layout.attrOffset[j]),
												0));
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
				}
				vertexDB[i] = itm;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void cpuFree() {
		this.bufferObject.cpuFree();
		this.vertexDB = null;
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
				if (layout.attrMapping[j].getType().isAssignableFrom(
						Matrix4.class)) {
					for (int r = 0; r < layout.attrSize[j]; r++)
						GL20.glVertexAttribPointer(j + r, layout.attrSize[j],
								layout.attrType[j], layout.attrNormalize[j],
								layout.structureSize, layout.attrOffset[j] + r
										* layout.attrSize[j] * 4);
				} else {
					GL20.glVertexAttribPointer(j, layout.attrSize[j],
							layout.attrType[j], layout.attrNormalize[j],
							layout.structureSize, layout.attrOffset[j]);
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
				int span = 1;
				Class<?> type = layout.attrMapping[j].getType();
				if (type.isArray())
					type = type.getComponentType();
				if (type.isAssignableFrom(Matrix4.class))
					span = 4;
				for (int r = 0; r < span; r++)
					GL20.glEnableVertexAttribArray(j + r);
			}
		}
	}

	public void deactive() {
		for (int j = 0; j < layout.attrMapping.length; j++) {
			if (layout.attrMapping[j] != null) {
				int span = 1;
				Class<?> type = layout.attrMapping[j].getType();
				if (type.isArray())
					type = type.getComponentType();
				if (type.isAssignableFrom(Matrix4.class))
					span = 4;
				for (int r = 0; r < span; r++)
					GL20.glDisableVertexAttribArray(j + r);
			}
		}
		GL30.glBindVertexArray(0);
	}

	@Override
	public int getID() {
		return vao;
	}
}
