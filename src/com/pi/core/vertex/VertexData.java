package com.pi.core.vertex;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import org.lwjgl.opengl.GL20;

import com.pi.core.buffers.BufferType;
import com.pi.core.buffers.GLGenericBuffer;
import com.pi.core.misc.VertexArrayObject;
import com.pi.core.util.GPUObject;
import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.ByteVector;
import com.pi.math.vector.VectorBuff;
import com.pi.math.volume.BoundingArea;

public class VertexData<E> extends GPUObject<VertexData<E>> {
	public E[] vertexDB;
	public final Class<E> vertexClass;
	private int count;
	private final VertexLayout layout;
	public final GLGenericBuffer bufferObject;
	private VertexArrayObject vao = new VertexArrayObject();

	private void init() {
		cpuAlloc();
	}

	public VertexData(Class<E> vertexClass, int count) {
		this.vertexClass = vertexClass;
		this.layout = new VertexLayout(vertexClass);
		this.count = count;
		this.layout.validate();
		this.bufferObject = new GLGenericBuffer(this.count * this.layout.structureSize);
		init();
	}

	public VertexData(Class<E> vertexClass, GLGenericBuffer data) {
		this.vertexClass = vertexClass;
		this.layout = new VertexLayout(vertexClass);
		this.count = data.size() / this.layout.structureSize;
		this.layout.validate();
		this.bufferObject = data;
		init();
	}

	@SuppressWarnings("unchecked")
	public VertexData<E> resize(int n) {
		int oc = this.count;
		this.count = n;
		if (oc < n || oc > n + 8) {
			this.bufferObject.resize(n * this.layout.structureSize);
			cpuAlloc();
		} else if (oc < n) {
			cpuAlloc();
		} else if (n < oc) {
			E[] tmp = vertexDB;
			this.vertexDB = (E[]) Array.newInstance(vertexClass, count);
			System.arraycopy(tmp, 0, this.vertexDB, 0, n);
		}
		return this;
	}

	public int vertexSize() {
		return layout.structureSize;
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
								array = Array.newInstance(type, attr.getAnnotation(AttrLayout.class).arraySize());
								attr.set(itm, array);
							}

							if (VectorBuff.class.isAssignableFrom(type)) {
								Array.set(array, layout.attrIndex[j],
										VectorBuff.make(this.bufferObject.floatImageAt(head + layout.attrOffset[j]), 0,
												layout.attrSize[j]));
							} else if (type.isAssignableFrom(Matrix4.class)) {
								Array.set(array, layout.attrIndex[j],
										new Matrix4(this.bufferObject.floatImageAt(head + layout.attrOffset[j]), 0));
							} else if (ByteVector.class.isAssignableFrom(type)) {
								Array.set(array, layout.attrIndex[j], ByteVector.make(this.bufferObject.getBacking(),
										head + layout.attrOffset[j], layout.attrSize[j]));
							}
						} else {
							if (VectorBuff.class.isAssignableFrom(attr.getType())) {
								attr.set(itm,
										VectorBuff.make(this.bufferObject.floatImageAt(head + layout.attrOffset[j]), 0,
												layout.attrSize[j]));
							} else if (attr.getType().isAssignableFrom(Matrix4.class)) {
								attr.set(itm,
										new Matrix4(this.bufferObject.floatImageAt(head + layout.attrOffset[j]), 0));
							} else if (ByteVector.class.isAssignableFrom(attr.getType())) {
								attr.set(itm, ByteVector.make(this.bufferObject.getBacking(),
										head + layout.attrOffset[j], layout.attrSize[j]));
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
	 * If you changed the vertex data you need to resync the buffer. This does
	 * that.
	 */
	@Override
	protected void gpuUploadInternal() {
		bufferObject.gpuUpload();
	}

	public void setupVertexParams() {
		bufferObject.bind(BufferType.ARRAY);
		for (int j = 0; j < layout.attrMapping.length; j++) {
			if (layout.attrMapping[j] != null) {
				if (layout.attrMapping[j].getType().isAssignableFrom(Matrix4.class)) {
					for (int r = 0; r < layout.attrSize[j]; r++)
						GL20.glVertexAttribPointer(j + r, layout.attrSize[j], layout.attrType[j],
								layout.attrNormalize[j], layout.structureSize,
								layout.attrOffset[j] + r * layout.attrSize[j] * 4);
				} else {
					GL20.glVertexAttribPointer(j, layout.attrSize[j], layout.attrType[j], layout.attrNormalize[j],
							layout.structureSize, layout.attrOffset[j]);
				}
			}
		}

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

	@Override
	protected void gpuAllocInternal() {
		// Dump the buffer
		vao.gpuAlloc();
		vao.bind();

		bufferObject.gpuAlloc();
		setupVertexParams();
		VertexArrayObject.unbind();
	}

	@Override
	protected void gpuFreeInternal() {
		vao.gpuFree();
		bufferObject.gpuFree();
	}

	public void activate() {
		vao.bind();
	}

	public static interface PositionVertex<E> {
		public VectorBuff position(E vtx);
	}

	public void include(BoundingArea area, PositionVertex<? super E> cpy) {
		for (int i = 0; i < vertexDB.length; i++)
			area.include(cpy.position(vertexDB[i]));
	}

	@Override
	public String toString() {
		return vertexClass.getSimpleName() + " x" + count;
	}
}
