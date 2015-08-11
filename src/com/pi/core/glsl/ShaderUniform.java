package com.pi.core.glsl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL31;

import com.pi.core.texture.Texture;
import com.pi.core.util.WarningManager;
import com.pi.math.matrix.Matrix3;
import com.pi.math.matrix.Matrix34;
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

		String name = GL20.glGetActiveUniform(prog.getID(), id, maxNameLength, sizeBuff, typeBuff);
		boolean array = name.endsWith("]");
		if (array)
			name = name.substring(0, name.length() - 3);

		this.prog = prog;
		this.name = name;
		this.size = sizeBuff.get(0);
		this.type = typeBuff.get(0);
		this.location = new int[array ? size : 1];

		this.uniformBlockIndex = GL31.glGetActiveUniformsi(prog.getID(), id, GL31.GL_UNIFORM_BLOCK_INDEX);
		if (uniformBlockIndex >= 0) {
			location[0] = GL31.glGetActiveUniformsi(prog.getID(), id, GL31.GL_UNIFORM_OFFSET);
			if (array) {
				int stride = GL31.glGetActiveUniformsi(prog.getID(), id, GL31.GL_UNIFORM_ARRAY_STRIDE);
				for (int i = 1; i < location.length; i++)
					location[i] = location[i - 1] + stride;
			}
		} else {
			if (array)
				for (int i = 0; i < size; i++) {
					location[i] = GL20.glGetUniformLocation(prog.getID(), name + "[" + i + "]");
				}
			else
				location[0] = GL20.glGetUniformLocation(prog.getID(), name);
		}

		for (int k = 0; k < location.length; k++)
			if (location[k] == -1)
				if (array)
					System.err.println("Unable to locate " + name + "[" + k + "]\t@" + location[k]);
				else
					System.err.println("Unable to locate " + name + "\t@" + location[k]);

		this.activeIndex = 0;
		this.samplerID = new int[this.location.length];
		Arrays.fill(this.samplerID, ShaderProgram.MAX_TEXTURE_UNITS);
	}

	public String name() {
		return name;
	}

	public ShaderUniform index(int i) {
		if (i < 0 || i >= size)
			throw new IllegalStateException(
					"Can't use index " + i + " on \"" + name + "\": It is an array of size " + size);
		this.activeIndex = i;
		return this;

	}

	public boolean defined() {
		return type != -1;
	}

	private final void utilAllowed() {
		if (WarningManager.GLSL_UNIFORM_TYPE_WATCHING && type == -1)
			throw new IllegalStateException(
					"Utility methods aren't allowed on uniform \"" + name + "\": It has an undefined type");
	}

	private final void typeMismatch(String provided) {
		throw new IllegalStateException("Uniform " + name + " isn't of type " + provided + ".");
	}

	private final IntBuffer intBuff = BufferUtils.createIntBuffer(4);
	private final FloatBuffer floatBuff = BufferUtils.createFloatBuffer(4);

	private final void commitIntsToUBO(int[] vals) {
		ShaderUniformBlock block = prog.uniformBlock(uniformBlockIndex);
		IntBuffer place = block.bound().integerImageAt(location[activeIndex]);
		for (int i = 0; i < vals.length; i++) {
			if (!ShaderUniformBlock.PERSISTENT_BUFFER_STATE || vals[i] != place.get(i)) {
				place.put(i, vals[i]);
				block.markDirty(location[activeIndex] + i * 4, location[activeIndex] + i * 4 + 4);
			}
		}
	}

	private final void commitIntsToUNF(int[] vals) {
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
			throw new IllegalArgumentException("Can't commit " + vals.length + " ints to a uniform");
		}
	}

	private final void commitInts(int... vals) {
		if (uniformBlockIndex >= 0)
			commitIntsToUBO(vals);
		else
			commitIntsToUNF(vals);
	}

	private final void commitFloatsToUBO(float[] vals) {
		ShaderUniformBlock block = prog.uniformBlock(uniformBlockIndex);
		FloatBuffer place = block.bound().floatImageAt(location[activeIndex]);
		if (ShaderUniformBlock.PERSISTENT_BUFFER_STATE) {
			for (int i = 0; i < vals.length; i++) {
				if (vals[i] != place.get(i)) {
					place.put(i, vals[i]);
					block.markDirty(location[activeIndex] + i * 4, location[activeIndex] + i * 4 + 4);
				}
			}
		} else
			place.put(vals);
	}

	private final void commitFloatsToUNF(float[] vals) {
		floatBuff.position(0);
		floatBuff.put(vals).flip();
		commitFloatsToUNF(floatBuff);
	}

	private final void commitFloats(float... vals) {
		if (uniformBlockIndex >= 0)
			commitFloatsToUBO(vals);
		else
			commitFloatsToUNF(vals);
	}

	public FloatBuffer uboAccessor() {
		ShaderUniformBlock block = prog.uniformBlock(uniformBlockIndex);
		return block.bound().floatImageAt(location[activeIndex]);
	}

	private final void commitFloatsToUBO(FloatBuffer f) {
		ShaderUniformBlock block = prog.uniformBlock(uniformBlockIndex);
		FloatBuffer place = block.bound().floatImageAt(location[activeIndex]);
		if (ShaderUniformBlock.PERSISTENT_BUFFER_STATE) {
			final int n = f.remaining();
			for (int i = 0; i < n; i++) {
				float val = f.get();
				if (val != place.get(i)) {
					place.put(i, val);
					block.markDirty(location[activeIndex] + i * 4, location[activeIndex] + i * 4 + 4);
				}
			}
		} else {
			place.put(f);
		}
	}

	private final void commitFloatsToUNF(FloatBuffer f) {
		switch (f.remaining()) {
		case 4:
			GL20.glUniform4(location[activeIndex], f);
			return;
		case 3:
			GL20.glUniform3(location[activeIndex], f);
			return;
		case 2:
			GL20.glUniform2(location[activeIndex], f);
			return;
		case 1:
			GL20.glUniform1(location[activeIndex], f);
			return;
		default:
			throw new IllegalArgumentException("Can't commit " + f.remaining() + " floats to a uniform");
		}
	}

	private final void commitFloats(FloatBuffer f) {
		if (uniformBlockIndex >= 0)
			commitFloatsToUBO(f);
		else
			commitFloatsToUNF(f);
	}

	public void texture(Texture t) {
		utilAllowed();
		if (WarningManager.GLSL_UNIFORM_TYPE_WATCHING && type != GL20.GL_SAMPLER_1D && type != GL20.GL_SAMPLER_1D_SHADOW
				&& type != GL20.GL_SAMPLER_2D && type != GL20.GL_SAMPLER_2D_SHADOW && type != GL20.GL_SAMPLER_3D
				&& type != GL20.GL_SAMPLER_CUBE)
			typeMismatch("sampler");

		if (this.samplerID[activeIndex] < ShaderProgram.MAX_TEXTURE_UNITS && this.samplerID[activeIndex] >= 0
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
				if (prog.textureUnit[i] == null && fNull == ShaderProgram.MAX_TEXTURE_UNITS) {
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
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL20.GL_BOOL_VEC2)
			commitInts(x ? 1 : 0, y ? 1 : 0);
		else
			typeMismatch("bool vec2");
	}

	public void bvector(boolean x, boolean y, boolean z) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL20.GL_BOOL_VEC3)
			commitInts(x ? 1 : 0, y ? 1 : 0, z ? 1 : 0);
		else
			typeMismatch("bool vec2");
	}

	public void bvector(boolean x, boolean y, boolean z, boolean w) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL20.GL_BOOL_VEC4)
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

	public void vector(float... vs) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || (vs.length == 1 && type == GL11.GL_FLOAT)
				|| (vs.length == 2 && type == GL20.GL_FLOAT_VEC2) || (vs.length == 3 && type == GL20.GL_FLOAT_VEC3)
				|| (vs.length == 4 && type == GL20.GL_FLOAT_VEC4))
			commitFloats(vs);
		else
			typeMismatch(vs.length == 1 ? "float" : ("float vec" + vs.length));
	}

	public void vector(Vector v) {
		if (v instanceof VectorBuff) {
			vector((VectorBuff) v);
			return;
		}
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
			throw new IllegalArgumentException(
					"Vectors of dimension " + v.dimension() + " can't be assigned to shader uniforms.");
		}
	}

	public void vector(VectorBuff v) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING)
			commitFloats(v.getAccessor());
		else {
			switch (v.dimension()) {
			case 4:
				if (type == GL20.GL_FLOAT_VEC4)
					commitFloats(v.getAccessor());
				else
					typeMismatch("float vec4");
				break;
			case 3:
				if (type == GL20.GL_FLOAT_VEC3)
					commitFloats(v.getAccessor());
				else
					typeMismatch("float vec3");
				break;
			case 2:
				if (type == GL20.GL_FLOAT_VEC2)
					commitFloats(v.getAccessor());
				else
					typeMismatch("float vec2");
				break;
			case 1:
				floating(v.get(0));
				break;
			default:
				throw new IllegalArgumentException(
						"Vectors of dimension " + v.dimension() + " can't be assigned to shader uniforms.");
			}
		}
	}

	public void matrix(Matrix4 m) {
		matrix(m, false);
	}

	public void matrix(Matrix4 m, boolean transpose) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL20.GL_FLOAT_MAT4) {
			if (uniformBlockIndex >= 0) {
				if (transpose)
					throw new IllegalStateException("Can't upload transposed matrix to UBO");
				commitFloatsToUBO(m.accessor());
			} else
				GL20.glUniformMatrix4(location[activeIndex], transpose, m.accessor());
		} else
			typeMismatch("float matrix4");
	}

	public void matrix(Matrix34 m) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL21.GL_FLOAT_MAT4x3) {
			if (uniformBlockIndex >= 0) {
				commitFloatsToUBO(m.accessor());
			} else
				GL21.glUniformMatrix3x4(location[activeIndex], false, m.accessor());
		} else
			typeMismatch("float matrix34");
	}

	public void matrix(Matrix3 m) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL20.GL_FLOAT_MAT3) {
			if (uniformBlockIndex >= 0) {
				commitFloatsToUBO(m.accessor());
			} else
				GL20.glUniformMatrix3(location[activeIndex], false, m.accessor());
		} else
			typeMismatch("float matrix34");
	}

	public void color(ByteVector4 c) {
		vector(c.get(0), c.get(1), c.get(2), c.get(3));
	}

	public int location() {
		return location[activeIndex];
	}
}
