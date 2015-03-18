package com.pi.core.glsl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.pi.core.texture.Texture;
import com.pi.core.util.WarningManager;
import com.pi.core.vertex.BufferColor;
import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.Vector;

@SuppressWarnings("unused")
public final class ShaderUniform {
	private final ShaderProgram prog;
	private final String name;
	private final int size;
	private final int type;
	private final int[] location;
	private final int[] samplerID;
	private int activeIndex;

	public ShaderUniform(ShaderProgram prog, String name, int size, int type,
			boolean array) {
		this.prog = prog;
		this.name = name;
		this.size = size;
		this.type = type;
		this.location = new int[array ? size : 1];
		if (array)
			for (int i = 0; i < size; i++) {
				location[i] = GL20.glGetUniformLocation(prog.getID(), name
						+ "[" + i + "]");
			}
		else
			location[0] = GL20.glGetUniformLocation(prog.getID(), name);

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

	public ShaderUniform(ShaderProgram prog, String name, int location) {
		this.prog = prog;
		this.name = name;
		this.size = 1;
		this.type = -1;
		this.location = new int[] { location };

		this.activeIndex = 0;
		this.samplerID = new int[size];
		Arrays.fill(samplerID, ShaderProgram.MAX_TEXTURE_UNITS);
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

	public void texture(Texture t) {
		utilAllowed();
		if (WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				&& type != GL20.GL_SAMPLER_1D
				&& type != GL20.GL_SAMPLER_1D_SHADOW
				&& type != GL20.GL_SAMPLER_2D
				&& type != GL20.GL_SAMPLER_2D_SHADOW
				&& type != GL20.GL_SAMPLER_3D && type != GL20.GL_SAMPLER_CUBE)
			typeMismatch("sampler");

		final int prevSampler = this.samplerID[activeIndex];

		if (prevSampler < prog.textureUnit.length) {
//			if (prog.textureUnit[prevSampler] == t)
//				return;
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
			int fNull = -1;
			for (int i = 0; i < prog.textureUnit.length; i++) {
				if (prog.textureUnit[i] == null && fNull == -1) {
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
				prog.textureUnit[fNull] = t;
				prog.textureUnitRefCount[fNull] = 1;
			}
		}
//		if (this.samplerID[activeIndex] != prevSampler)
			GL20.glUniform1i(this.location[activeIndex],
					this.samplerID[activeIndex]);
	}

	public void bool(boolean b) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL20.GL_BOOL)
			GL20.glUniform1i(location[activeIndex], b ? 1 : 0);
		else
			typeMismatch("boolean");
	}

	public void bvector(boolean x, boolean y) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_BOOL_VEC2)
			GL20.glUniform2i(location[activeIndex], x ? 1 : 0, y ? 1 : 0);
		else
			typeMismatch("bool vec2");
	}

	public void bvector(boolean x, boolean y, boolean z) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_BOOL_VEC3)
			GL20.glUniform3i(location[activeIndex], x ? 1 : 0, y ? 1 : 0, z ? 1
					: 0);
		else
			typeMismatch("bool vec2");
	}

	public void bvector(boolean x, boolean y, boolean z, boolean w) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_BOOL_VEC4)
			GL20.glUniform4i(location[activeIndex], x ? 1 : 0, y ? 1 : 0, z ? 1
					: 0, w ? 1 : 0);
		else
			typeMismatch("bool vec4");
	}

	public void integer(int x) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL11.GL_INT)
			GL20.glUniform1i(location[activeIndex], x);
		else
			typeMismatch("int");
	}

	public void floating(float x) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING || type == GL11.GL_FLOAT)
			GL20.glUniform1f(location[activeIndex], x);
		else
			typeMismatch("float");
	}

	public void vector(float x, float y) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_FLOAT_VEC2)
			GL20.glUniform2f(location[activeIndex], x, y);
		else
			typeMismatch("float vec2");
	}

	public void vector(float x, float y, float z) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_FLOAT_VEC3)
			GL20.glUniform3f(location[activeIndex], x, y, z);
		else
			typeMismatch("float vec3");
	}

	public void vector(float x, float y, float z, float w) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_FLOAT_VEC4)
			GL20.glUniform4f(location[activeIndex], x, y, z, w);
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

	public void matrix(Matrix4 m) {
		matrix(m, false);
	}

	public void matrix(Matrix4 m, boolean transpose) {
		utilAllowed();
		if (!WarningManager.GLSL_UNIFORM_TYPE_WATCHING
				|| type == GL20.GL_FLOAT_MAT4)
			GL20.glUniformMatrix4(location[activeIndex], transpose,
					m.getAccessor());
		else
			typeMismatch("float matrix4");
	}

	public void color(BufferColor c) {
		ByteBuffer buff = c.getAccessor();
		vector((buff.get(0) & 0xFF) / 255f, (buff.get(1) & 0xFF) / 255f,
				(buff.get(2) & 0xFF) / 255f, (buff.get(3) & 0xFF) / 255f);

	}

	public int location() {
		return location[activeIndex];
	}
}
