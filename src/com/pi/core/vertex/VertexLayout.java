package com.pi.core.vertex;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.ByteVector;
import com.pi.math.vector.ByteVector4;
import com.pi.math.vector.VectorBuff;
import com.pi.math.vector.VectorBuff2;
import com.pi.math.vector.VectorBuff3;
import com.pi.math.vector.VectorBuff4;

class VertexLayout {
	private static final int MAX_ATTR_COUNT = 64; // Realistically 16 on most
													// GPUs

	public final int structureSize;
	public final Field[] attrMapping;
	public final int[] attrOffset, attrSize, attrType, attrIndex;
	public final boolean[] attrNormalize;

	private static void getFields(List<Field> fields, Class<?> clazz) {
		for (Field f : clazz.getDeclaredFields())
			fields.add(f);
		if (clazz.getSuperclass() != null)
			getFields(fields, clazz.getSuperclass());
	}

	public VertexLayout(Class<?> clazz) {
		int structSize = 0;
		attrMapping = new Field[MAX_ATTR_COUNT];
		attrOffset = new int[MAX_ATTR_COUNT];
		attrSize = new int[MAX_ATTR_COUNT];
		attrType = new int[MAX_ATTR_COUNT];
		attrNormalize = new boolean[MAX_ATTR_COUNT];
		attrIndex = new int[MAX_ATTR_COUNT];

		List<Field> fields = new ArrayList<>();
		getFields(fields, clazz);
		// Sorts fields by layout for consistent data layout accross different
		// computers
		fields.sort(new Comparator<Field>() {
			@Override
			public int compare(Field a, Field b) {
				AttrLayout al = a.getAnnotation(AttrLayout.class);
				AttrLayout bl = b.getAnnotation(AttrLayout.class);
				if (al == null && bl == null)
					return 0;
				if (al == null)
					return -1;
				if (bl == null)
					return 1;
				return Integer.compare(al.layout(), bl.layout());
			}
		});

		for (Field f : fields) {
			AttrLayout layout = f.getAnnotation(AttrLayout.class);
			if (layout != null) {
				Class<?> type = f.getType();
				if (layout.arraySize() < 0)
					throw new NegativeArraySizeException(
							"Array size of " + f.getName() + " is less than zero.  This will never work.");
				if (layout.arraySize() != 1 && !type.isArray())
					throw new IllegalArgumentException(
							"Array size of non-array type " + f.getName() + " not one.  This will never work");

				if (type.isArray())
					type = type.getComponentType();

				for (int k = 0; k < layout.arraySize(); k++) {
					int attrID = layout.layout() + k * (type.isAssignableFrom(Matrix4.class) ? 4 : 1);
					attrOffset[attrID] = structSize;
					attrIndex[attrID] = k;
					if (VectorBuff.class.isAssignableFrom(type)) {
						int dimension = -1;
						if (VectorBuff2.class.isAssignableFrom(type))
							dimension = 2;
						else if (VectorBuff3.class.isAssignableFrom(type))
							dimension = 3;
						else if (VectorBuff4.class.isAssignableFrom(type))
							dimension = 4;
						if (layout.dimension() >= 0)
							dimension = layout.dimension();
						if (dimension < 2 || dimension > 4)
							throw new IllegalArgumentException(
									"A vector style vertex attr may only have 2-4 components.  (" + f.getName()
											+ ")  You likely have to define the AttrLayout#dimension() parameter.");
						structSize += 4 * dimension;
						attrType[attrID] = GL11.GL_FLOAT;
						attrSize[attrID] = dimension;
						attrNormalize[attrID] = false;
					} else if (type.isAssignableFrom(Matrix4.class)) {
						if (layout.dimension() >= 0 && layout.dimension() != 4)
							throw new UnsupportedOperationException("Non 4-D matricies aren't supported.");
						structSize += 16 * 4;
						attrSize[attrID] = 4;
						attrType[attrID] = GL11.GL_FLOAT;
						attrNormalize[attrID] = false;
					} else if (ByteVector.class.isAssignableFrom(type)) {
						int dimension = -1;
						if (ByteVector4.class.isAssignableFrom(type))
							dimension = 4;
						if (layout.dimension() >= 0)
							dimension = layout.dimension();
						if (dimension != 4 && dimension != 3)
							throw new UnsupportedOperationException("Non 3/4-D colors aren't supported.");
						structSize += 4;
						attrSize[attrID] = dimension;
						attrType[attrID] = GL11.GL_UNSIGNED_BYTE;
						attrNormalize[attrID] = true;
					} else {
						System.err.println("Warning: You tried to mark " + f.getName() + " of type "
								+ type.getSimpleName() + " as a vertex attr.  It wasn't recognized.");
						continue;
					}
					if (attrMapping[attrID] != null)
						throw new IllegalStateException(
								"Attribute " + f.getName() + " collides with " + attrMapping[attrID].getName());
					attrMapping[attrID] = f;
				}
			}
		}
		this.structureSize = structSize;
	}

	public void validate() {
		int maxID;
		if (GLFW.glfwGetCurrentContext() != 0)
			maxID = GL11.glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS);
		else
			maxID = MAX_ATTR_COUNT;
		// The maximum number of vIDs
		for (int i = maxID; i < attrMapping.length; i++)
			if (attrMapping[i] != null)
				throw new UnsupportedOperationException(
						"Attribute " + attrMapping[i].getName() + " is over the maximum attr count of " + maxID);

		for (int i = 0; i < MAX_ATTR_COUNT; i++) {
			if (attrMapping[i] != null) {
				Class<?> type = attrMapping[i].getType();
				if (type.isArray())
					type = type.getComponentType();
				int registerCount = type.isAssignableFrom(Matrix4.class) ? attrSize[i] : 1;

				for (int r = 0; r < registerCount; r++) {
					if (i + r > maxID)
						throw new ArrayIndexOutOfBoundsException("An attribute (" + attrMapping[i].getName() + "["
								+ attrIndex[i] + "]) overflowed the attribute registers (x" + maxID + ")");

					if (attrMapping[i + r] != null && r > 0)
						throw new ArrayIndexOutOfBoundsException("The attribute " + attrMapping[i + r].getName()
								+ " collides with the attribute " + attrMapping[i].getName());
				}
			}
		}
	}
}
