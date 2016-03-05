package com.pi.core.vertex;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;

import org.lwjgl.opengl.GL20;

import com.pi.core.GLException;
import com.pi.core.buffers.BufferType;
import com.pi.core.buffers.GLGenericBuffer;
import com.pi.core.misc.VertexArrayObject;
import com.pi.core.util.GPUObject;
import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.ByteVector;
import com.pi.math.vector.VectorBuff;
import com.pi.math.volume.BoundingArea;

public class VertexData<E> extends GPUObject<VertexData<E>> implements Iterable<E> {
	private E[] vertexDB;
	public final Class<E> vertexClass;
	private int capacity;
	private int count;
	private final VertexLayout layout;
	public final GLGenericBuffer bufferObject;
	private VertexArrayObject vao = new VertexArrayObject();

	public VertexData(Class<E> vertexClass, GLGenericBuffer data) {
		this.vertexClass = vertexClass;
		this.layout = new VertexLayout(vertexClass);
		this.count = data.size() / this.layout.structureSize;
		this.capacity = count;
		this.layout.validate();
		this.bufferObject = data;
		init();
	}

	public VertexData(Class<E> vertexClass, int count) {
		this.vertexClass = vertexClass;
		this.layout = new VertexLayout(vertexClass);
		this.count = count;
		this.capacity = count;
		this.layout.validate();
		this.bufferObject = new GLGenericBuffer(this.count * this.layout.structureSize);
		init();
	}

	public void activate() {
		vao.bind();
	}

	public int count() {
		return count;
	}

	@SuppressWarnings("unchecked")
	public void cpuAlloc() {
		this.bufferObject.cpuAlloc();
		this.vertexDB = (E[]) Array.newInstance(vertexClass, capacity);
		populate(0, capacity);
	}

	public void cpuFree() {
		this.bufferObject.cpuFree();
		this.vertexDB = null;
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

	/**
	 * If you changed the vertex data you need to resync the buffer. This does
	 * that.
	 */
	@Override
	protected void gpuUploadInternal() {
		bufferObject.gpuUpload();
	}

	public void include(BoundingArea area, PositionVertex<? super E> cpy) {
		for (int i = 0; i < vertexDB.length; i++)
			area.include(cpy.position(vertexDB[i]));
	}

	private void init() {
		cpuAlloc();
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			int head = 0;

			@Override
			public boolean hasNext() {
				return head < count;
			}

			@Override
			public E next() {
				return vertexDB[head++];
			}
		};
	}

	private void populate(int left, int right) {
		for (int i = left; i < right; i++) {
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
				throw new GLException("Unable to create VertexData", e);
			}
		}
	}

	public VertexData<E> resize(int n, int pad) {
		int oc = vertexDB.length;
		this.count = n;
		if (oc < n || oc > n + pad) {
			this.capacity = n + pad;
			this.bufferObject.resize(capacity * this.layout.structureSize, 0);
			cpuAlloc();
		}
		return this;
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
	public String toString() {
		return vertexClass.getSimpleName() + " x" + count;
	}

	public E v(int i) {
		return vertexDB[i];
	}

	public int vertexSize() {
		return layout.structureSize;
	}

	public static interface PositionVertex<E> {
		public VectorBuff position(E vtx);
	}
}
