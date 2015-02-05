package com.pi.gl.mesh;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.VectorBuff;

public class VertexLayout {
	private static final int MAX_ATTR_COUNT = 64; // Realistically 16 on most GPUs

	final int byteSize;
	final Field[] attrMapping;
	final int[] attrOffset, attrSize, attrType;

	public VertexLayout(Class<?> clazz) {
		int structSize = 0;
		attrMapping = new Field[MAX_ATTR_COUNT];
		attrOffset = new int[MAX_ATTR_COUNT];
		attrSize = new int[MAX_ATTR_COUNT];
		attrType = new int[MAX_ATTR_COUNT];

		for (Field f : clazz.getDeclaredFields()) {
			AttrLayout layout = f.getAnnotation(AttrLayout.class);
			if (layout != null) {
				Class<?> type = f.getType();
				int attrID = layout.layout();
				attrOffset[attrID] = structSize;
				if (type.isAssignableFrom(VectorBuff.class)) { // Vector type
					if (layout.dimension() < 2 || layout.dimension() > 4)
						throw new RuntimeException(
								"A vector style vertex attr may only have 2-4 components.  ("
										+ f.getName()
										+ ")  You likely have to define the AttrLayout#dimension() parameter.");
					structSize += 4 * layout.dimension();
					attrType[attrID] = GL11.GL_FLOAT;
					attrSize[attrID] = layout.dimension();
				}/*
				 * else if (type.isPrimitive()) { // Primitive type structSize += PrimitiveInfo.sizeof(type); attrSize[attrID] = 1;
				 * attrType[attrID] = }
				 */else if (type.isAssignableFrom(Matrix4.class)) {
					if (layout.dimension() >= 0 && layout.dimension() != 4
							&& layout.dimension() != 3)
						throw new RuntimeException(
								"Non 3/4-D matricies aren't supported.");
					structSize += 16 * 4;
					attrSize[attrID] = layout.dimension() >= 0 ? layout
							.dimension() : 4;
					attrType[attrID] = GL11.GL_FLOAT;
				} else {
					System.err.println("Warning: You tried to mark "
							+ f.getName() + " of type " + type.getSimpleName()
							+ " as a vertex attr.  It wasn't recognized.");
					continue;
				}
				if (attrMapping[attrID] != null)
					throw new RuntimeException("Attribute " + f.getName()
							+ " collides with " + attrMapping[attrID].getName());
				attrMapping[attrID] = f;
			}
		}
		this.byteSize = structSize;
	}

	public void validate() {
		int maxID = GL11.glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS);
		// The maximum number of vIDs
		for (int i = maxID; i < attrMapping.length; i++)
			if (attrMapping[i] != null)
				throw new RuntimeException("Attribute "
						+ attrMapping[i].getName()
						+ " is over the maximum attr count of " + maxID);

		for (int i = 0; i < maxID; i++) {
			if (attrMapping[i] != null
					&& attrMapping[i].getType().isAssignableFrom(Matrix4.class)) {
				// If it is a matrix the next 3 have to be empty.
				if (i + 3 >= maxID)
					throw new RuntimeException("A matrix attribute ("
							+ attrMapping[i].getName()
							+ ") overflowed the attribute register count "
							+ maxID);
				for (int j = i; j < i + attrSize[i]; j++)
					if (attrMapping[j] != null)
						throw new RuntimeException("The attribute "
								+ attrMapping[j].getName()
								+ " collides with the matrix attribute "
								+ attrMapping[i].getName());
			}
		}
	}
}
