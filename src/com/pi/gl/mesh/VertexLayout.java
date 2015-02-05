package com.pi.gl.mesh;

import java.lang.reflect.Field;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.pi.gl.util.PrimitiveSize;
import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.VectorBuff;

public class VertexLayout {
	private static final int MAX_ATTR_COUNT = 64; // Realistically 16 on most
													// GPUs

	private final int byteSize;
	private final Field[] attrMapping;

	public VertexLayout(Class<?> clazz) {
		int size = 0;
		Field[] attrMap = new Field[MAX_ATTR_COUNT];

		for (Field f : clazz.getDeclaredFields()) {
			AttrLayout layout = f.getAnnotation(AttrLayout.class);
			if (layout != null) {
				Class<?> type = f.getType();
				if (type.isAssignableFrom(VectorBuff.class)) { // Vector type
					if (layout.dimension() < 2 || layout.dimension() > 4)
						throw new RuntimeException(
								"A vector style vertex attr may only have 2-4 components.  ("
										+ f.getName()
										+ ")  You likely have to define the AttrLayout#dimension() parameter.");
					size += 4 * layout.dimension();
				} else if (type.isPrimitive()) { // Primitive type
					size += PrimitiveSize.sizeof(type);
				} else if (type.isAssignableFrom(Matrix4.class)) {
					if (layout.dimension() >= 0 && layout.dimension() != 4)
						throw new RuntimeException(
								"Non 4-D matricies aren't supported.");
					size += 16 * 4;
				} else {
					System.err.println("Warning: You tried to mark "
							+ f.getName() + " of type " + type.getSimpleName()
							+ " as a vertex attr.  It wasn't recognized.");
					continue;
				}
				if (attrMap[layout.layout()] != null)
					throw new RuntimeException("Attribute " + f.getName()
							+ " collides with "
							+ attrMap[layout.layout()].getName());
				attrMap[layout.layout()] = f;
			}
		}
		this.attrMapping = attrMap;
		this.byteSize = size;
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
				for (int j = i; j < i + 4; j++)
					if (attrMapping[j] != null)
						throw new RuntimeException("The attribute "
								+ attrMapping[j].getName()
								+ " collides with the matrix attribute "
								+ attrMapping[i].getName());
			}
		}
	}
}
