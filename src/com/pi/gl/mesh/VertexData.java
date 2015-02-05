package com.pi.gl.mesh;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.pi.gl.buffers.GLGenericBuffer;
import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.VectorBuff;

public class VertexData<E> {
	public final E[] vertexDB;
	private GLGenericBuffer bufferObject;
	private final VertexLayout layout;

	@SuppressWarnings("unchecked")
	public VertexData(Class<E> vertexClass, int count) {
		this.vertexDB = (E[]) Array.newInstance(vertexClass, count);
		this.layout = new VertexLayout(vertexClass);
		this.layout.validate();
		this.bufferObject = new GLGenericBuffer(layout.byteSize * count);
		// Allocate and link to the GL Generic Buffer.
		for (int i = 0; i < count; i++) {
			try {
				E itm = vertexClass.newInstance();
				int head = i * layout.byteSize;
				for (int j = 0; j < layout.attrMapping.length; j++) {
					if (layout.attrMapping[j] != null) {
						layout.attrMapping[j].setAccessible(true);
						if (layout.attrMapping[j].getType().isAssignableFrom(
								VectorBuff.class)) {
							layout.attrMapping[j].set(
									itm,
									new VectorBuff(this.bufferObject
											.floatImageAt(head
													+ layout.attrOffset[j]), 0,
											layout.attrSize[j]));
						} else if (layout.attrMapping[j].getType()
								.isAssignableFrom(Matrix4.class)) {
							layout.attrMapping[j]
									.set(itm,
											new Matrix4(
													this.bufferObject
															.floatImageAt(head
																	+ layout.attrOffset[j]),
													0));
						}
					}
				}
				vertexDB[i] = itm;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private int vao;

	public void loadToGPU() {
		// Dump the buffer
		vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);

		bufferObject.gpuAlloc();
		bufferObject.syncToGPU();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferObject.getID());

		for (int j = 0; j < layout.attrMapping.length; j++) {
			if (layout.attrMapping[j] != null) {
				AttrLayout lay = layout.attrMapping[j]
						.getAnnotation(AttrLayout.class);
				if (layout.attrMapping[j].getType().isAssignableFrom(
						Matrix4.class)) {
					for (int r = 0; r < layout.attrSize[j]; r++)
						GL20.glVertexAttribPointer(lay.layout() + r,
								layout.attrSize[j], layout.attrType[j], false,
								layout.byteSize, layout.attrOffset[j] + r
										* layout.attrSize[j] * 4);
				} else {
					System.out.println("Args: " + lay.layout() + ","
							+ layout.attrSize[j] + "," + layout.attrType[j]
							+ "," + false + "," + layout.byteSize + ","
							+ layout.attrOffset[j]);
					GL20.glVertexAttribPointer(lay.layout(),
							layout.attrSize[j], layout.attrType[j], false,
							layout.byteSize, layout.attrOffset[j]);
				}
			}
		}

		GL30.glBindVertexArray(0);
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
}
