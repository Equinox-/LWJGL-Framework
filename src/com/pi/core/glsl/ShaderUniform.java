package com.pi.core.glsl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

import com.pi.core.texture.Texture;
import com.pi.core.util.WarningManager;
import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.ByteVector4;
import com.pi.math.vector.Vector;
import com.pi.math.vector.VectorBuff;

public final class ShaderUniform {
	private final ShaderProgram prog;
	private final String name;
	private final int size;
	private final int type;
	private final int[] location;
	private final int[] samplerID;
	private final int uniformBlockIndex;
	private int activeIndex;

	public ShaderUniform(ShaderProgram prog, int id, int maxNameLength) {
		IntBuffer sizeBuff = BufferUtils.createIntBuffer(1);
		IntBuffer typeBuff = BufferUtils.createIntBuffer(1);

		String name = GL20.glGetActiveUniform(prog.getID(), id, maxNameLength,
				sizeBuff, typeBuff);
		boolean array = name.endsWith("]");
		if (array)
			name = name.substring(0, name.length() - 3);

		this.prog = prog;
		this.name = name;
		this.size = sizeBuff.get(0);
		this.type = typeBuff.get(0);
		this.location = new int[array ? size : 1];

		this.uniformBlockIndex = GL31.glGetActiveUniformsi(prog.getID(), id,
				GL31.GL_UNIFORM_BLOCK_INDEX);
		if (uniformBlockIndex >= 0) {
			location[0] = GL31.glGetActiveUniformsi(prog.getID(), id,
					GL31.GL_UNIFORM_OFFSET);
			if (array) {
				int stride = GL31.glGetActiveUniformsi(prog.getID(), id,
						GL31.GL_UNIFORM_ARRAY_STRIDE);
				for (int i = 1; i < location.length; i++)
					location[i] = location[i - 1] + stride;
			}
			System.out.println("UBO Offsets for " + name + ": " + Arrays.toString(location));
		} else {
			if (array)
				for (int i = 0; i < size; i++) {
					location[i] = GL20.glGetUniformLocation(prog.getID(), name
							+ "[" + i + "]");
				}
			else
				location[0] = GL20.glGetUniformLocation(prog.getID(), name);
		}

		for (int k = 0; k < location.length; k++)
			if (location[k] == -1)
				if (array)
					System.err.println("Unable to locate " + name + "[" + k
							+ "]\t@" + location[k]);
				else
					System.err.println("Unable to locate " + name + "\t@"
							+ location[k]);

		this.activeIndex = 0;
		this.samplerID = new int[this.location.length];
		Arrays.fill(this.samplerID, ShaderProgram.MAX_TEXTURE_UNITS);
	}

	public String name() {
		return name;
	}

	public ShaderUniform index(int i) {
		if (i < 0 || i >= size)
			throw new IllegalStateException("Can't use index " + i + " on \""
					+ name + "\": It is an array of size " + size);
		this.activeIndex = i;
		return this;

	}

	public boolean defined() {
		return type != -1;
	}

	private final void utilAllowed() {
		if (WarningManager.GLSL_UNIFORM_TYPE_WATCHING && type == -1)
			throw new IllegalStateException(
					"Utility methods aren't allowed on uniform \"" + name
							+ "\": It has an undefined type");
	}

	private final void typeMismatch(String provided) {
		throw new IllegalStateException("Uniform " + name + " isn't of type "
				+ provided + ".");
	}

	private final IntBuffer intBuff = BufferUtils.createIntBuffer(4);
	private final FloatBuffer floatBuff = BufferUtils.createFloatBuffer(4);

	private void commitInts(int... vals) {
		if (uniformBlockIndex >= 0) {
			prog.uniformBlock(uniformBlockIndex).dirty = true;
			IntBuffer place = prog.uniformBlock(uniformBlockIndex).bound()
					.integerImageAt(location[activeIndex]);
			place.put(vals);
		} else {
			intBuff.position(0);
			intBuff.put(vals).flip();
			switch (vals.length) {
			case 4:
				GL20.glUniform4(location[activeIndex], intBuff);
				break;
			case 3:
				GL20.glUniform3(location[activeIndex], intBuff);
				break;
			case 2:
				GL20.glUniform2(location[activeIndex], intBuff);
				break;
			case 1:
				GL20.glUniform1(location[activeIndex], intBuff);
				break;
			default:
				throw new IllegalArgumentException("Can't commit "
						+ vals.length + " ints to a uniform");
			}
		}
	}

	private void commitFloats(float... vals) {
		if (uniformBlockIndex >= 0) {
			prog.uniformBlock(uniformBlockIndex).dirty = true;
			FloatBuffer place = prog.uniformBlock(uniformBlockIndex).bound()
					.floatImageAt(location[activeIndex]);
			place.put(vals);
		} else {
			floatBuff.position(0);
			floatBuff.put(vals).flip();
			switch (vals.length) {
			case 4:
				GL20.glUniform4(location[activeIndex], floatBuff);
				break;
			case 3:
				GL20.glUniform3(location[activeIndex], floatBuff);
				break;
			case 2:
				GL20.glUniform2(location[activeIndex], floatBuff);
				break;
			case 1:
				GL20.glUniform1(location[activeIndex], floatBuff);
				break;
			default:
				throw new IllegalArgumentException("Can't commit "
						+ vals.length + " floats to a uniform");
			}
		}
	}

	private void commitFloats(FloatBuffer f) {
		if (uniformBlockIndex >= 0) {
			prog.uniformBlock(uniformBlockIndex).dirty = true;
			FloatBuffer place = prog.uniformBlock(uniformBlockIndex).bound()
					.floatImageAt(location[activeIndex]);
			place.put(f);
		} else {
			switch (f.limit()) {
			case 4:
				GL20.glUniform4(location[activeIndex], f);
				break;
			case 3:
				GL20.glUniform3(location[activeIndex], f);
				break;
			case 2:
				GL20.glUniform2(location[activeIndex], f);
				break;
			case 1:
				GL20.glUniform1(location[activeIndex], f);
				break;
			default:
				throw new IllegalArgumentException("Can't commit " + f.limit()
						+ " floats to a uniform");
			}
		}
	}

	public void texture(Texture t) {
		utilAllowed();
		if (WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				&& type != GL20.GL_SAMPLER_1D
				&& type != GL20.GL_SAMPLER_1D_SHADOW
				&& type != GL20.GL_SAMPLER_2D
				&& type != GL20.GL_SAMPLER_2D_SHADOW
				&& type != GL20.GL_SAMPLER_3D && type != GL20.GL_SAMPLER_CUBE)
			typeMismatch("sampler");

		if (this.samplerID[activeIndex] < ShaderProgram.MAX_TEXTURE_UNITS
				&& this.samplerID[activeIndex] >= 0
				&& prog.textureUnit[this.samplerID[activeIndex]] == t)
			return;

		final int prevSampler = this.samplerID[activeIndex];

		if (prevSampler < prog.textureUnit.length) {
			// if (prog.textureUnit[prevSampler] == t)
			// return;
			if (--prog.textureUnitRefCount[prevSampler] <= 0) {
				prog.textureUnit[prevSampler] = null;
				prog.textureUnitRefCount[prevSampler] = 0;
			}
		}
		if (t == null) {
			this.samplerID[activeIndex] = ShaderProgram.MAX_TEXTURE_UNITS;
		} else {
			// Search for an equiv texture object:
			boolean found = false;
			int fNull = ShaderProgram.MAX_TEXTURE_UNITS;
			for (int i = 0; i < prog.textureUnit.length; i++) {
				if (prog.textureUnit[i] == null
						&& fNull == ShaderProgram.MAX_TEXTURE_UNITS) {
					fNull = i;
				} else if (prog.textureUnit[i] == t) {
					this.samplerID[activeIndex] = i;
					prog.textureUnitRefCount[i]++;
					found = true;
					break;
				}
			}
			if (!found) {
				this.samplerID[activeIndex] = fNull;
				if (fNull == ShaderProgram.MAX_TEXTURE_UNITS) {
					System.err.println("Exceeded MAX TEXTURE UNITS");
				} else {
					prog.textureUnit[fNull] = t;
					prog.textureUnitRefCount[fNull] = 1;
				}
			}
		}
		commitInts(this.samplerID[activeIndex]);
	}

	public void bool(boolean b) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL20.GL_BOOL)
			commitInts(b ? 1 : 0);
		else
			typeMismatch("boolean");
	}

	public void bvector(boolean x, boolean y) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_BOOL_VEC2)
			commitInts(x ? 1 : 0, y ? 1 : 0);
		else
			typeMismatch("bool vec2");
	}

	public void bvector(boolean x, boolean y, boolean z) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_BOOL_VEC3)
			commitInts(x ? 1 : 0, y ? 1 : 0, z ? 1 : 0);
		else
			typeMismatch("bool vec2");
	}

	public void bvector(boolean x, boolean y, boolean z, boolean w) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_BOOL_VEC4)
			commitInts(x ? 1 : 0, y ? 1 : 0, z ? 1 : 0, w ? 1 : 0);
		else
			typeMismatch("bool vec4");
	}

	public void integer(int x) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL11.GL_INT)
			commitInts(x);
		else
			typeMismatch("int");
	}

	public void floating(float x) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL11.GL_FLOAT)
			commitFloats(x);
		else
			typeMismatch("float");
	}

	public void vector(float x, float y) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_FLOAT_VEC2)
			commitFloats(x, y);
		else
			typeMismatch("float vec2");
	}

	public void vector(float x, float y, float z) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_FLOAT_VEC3)
			commitFloats(x, y, z);
		else
			typeMismatch("float vec3");
	}

	public void vector(float x, float y, float z, float w) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_FLOAT_VEC4)
			commitFloats(x, y, z, w);
		else
			typeMismatch("float vec4");
	}

	public void vector(Vector v) {
		switch (v.dimension()) {
		case 4:
			vector(v.get(0), v.get(1), v.get(2), v.get(3));
			break;
		case 3:
			vector(v.get(0), v.get(1), v.get(2));
			break;
		case 2:
			vector(v.get(0), v.get(1));
			break;
		case 1:
			floating(v.get(0));
			break;
		default:
			throw new IllegalArgumentException("Vectors of dimension "
					+ v.dimension() + " can't be assigned to shader uniforms.");
		}
	}

	public void vector(VectorBuff v) {
		utilAllowed();
		switch (v.dimension()) {
		case 4:
			if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
					|| type == GL20.GL_FLOAT_VEC4)
				commitFloats(v.getAccessor());
			else
				typeMismatch("float vec4");
			break;
		case 3:
			if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
					|| type == GL20.GL_FLOAT_VEC3)
				commitFloats(v.getAccessor());
			else
				typeMismatch("float vec3");
			break;
		case 2:
			if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
					|| type == GL20.GL_FLOAT_VEC2)
				commitFloats(v.getAccessor());
			else
				typeMismatch("float vec2");
			break;
		case 1:
			floating(v.get(0));
			break;
		default:
			throw new IllegalArgumentException("Vectors of dimension "
					+ v.dimension() + " can't be assigned to shader uniforms.");
		}
	}

	public void matrix(Matrix4 m) {
		matrix(m, false);
	}

	public void matrix(Matrix4 m, boolean transpose) {
		utilAllowed();
		// TODO making this work with UBOs
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_FLOAT_MAT4)
			GL20.glUniformMatrix4(location[activeIndex], transpose,
					m.getAccessor());
		else
			typeMismatch("float matrix4");
	}

	public void color(ByteVector4 c) {
		vector(c);
	}

	public int location() {
		return location[activeIndex];
	}
}
