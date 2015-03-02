package com.pi.core.glsl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.pi.core.texture.Texture;
import com.pi.core.vertex.BufferColor;
import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.Vector;

public class ShaderUniform {
	private final ShaderProgram prog;
	private final String name;
	private final int size;
	private final int type;
	private final int location;

	private int samplerID;

	public ShaderUniform(ShaderProgram prog, String name, int size, int type,
			int location) {
		this.prog = prog;
		this.name = name;
		this.size = size;
		this.type = type;
		this.location = location;

		this.samplerID = -1;
	}

	private void utilAllowed() {
		if (type == -1)
			throw new IllegalStateException(
					"Utility methods aren't allowed on uniform \"" + name
							+ "\": It has an undefined type");
		if (size != 1)
			throw new IllegalStateException(
					"Utility methods aren't allowed on uniform \"" + name
							+ "\": It is an array");
	}

	private void typeMismatch(String provided) {
		throw new IllegalStateException("Uniform " + name + " isn't of type "
				+ provided + ".");
	}

	public void texture(Texture t) {
		utilAllowed();
		if (type != GL20.GL_SAMPLER_1D && type != GL20.GL_SAMPLER_1D_SHADOW
				&& type != GL20.GL_SAMPLER_2D
				&& type != GL20.GL_SAMPLER_2D_SHADOW
				&& type != GL20.GL_SAMPLER_3D && type != GL20.GL_SAMPLER_CUBE)
			typeMismatch("sampler");

		if (this.samplerID >= 0)
			prog.samplers[this.samplerID] = null;
		if (t == null) {
			this.samplerID = -1;
		} else {
			for (int i = 0; i < prog.samplers.length; i++) {
				if (prog.samplers[i] == null) {
					this.samplerID = i;
					prog.samplers[i] = t;
					break;
				}
			}
		}
		GL20.glUniform1i(location, samplerID);
	}

	public void bool(boolean b) {
		utilAllowed();
		if (type == GL20.GL_BOOL)
			GL20.glUniform1i(location, b ? 1 : 0);
		else
			typeMismatch("boolean");
	}

	public void scalar(float x) {
		utilAllowed();
		if (type == GL11.GL_FLOAT)
			GL20.glUniform1f(location, x);
		else if (type == GL11.GL_INT || type == GL20.GL_BOOL)
			GL20.glUniform1i(location, (int) x);
		else
			typeMismatch("float or int");
	}

	public void vector(float x, float y) {
		utilAllowed();
		if (type == GL20.GL_FLOAT_VEC2)
			GL20.glUniform2f(location, x, y);
		else
			typeMismatch("float vec2");
	}

	public void vector(float x, float y, float z) {
		utilAllowed();
		if (type == GL20.GL_FLOAT_VEC3)
			GL20.glUniform3f(location, x, y, z);
		else
			typeMismatch("float vec3");
	}

	public void vector(float x, float y, float z, float w) {
		utilAllowed();
		if (type == GL20.GL_FLOAT_VEC4)
			GL20.glUniform4f(location, x, y, z, w);
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
			scalar(v.get(0));
			break;
		default:
			throw new IllegalArgumentException("Vectors of dimension "
					+ v.dimension() + " can't be assigned to shader uniforms.");
		}
	}

	private FloatBuffer mat3TmpBuffer;
	private float[] mat4TmpBuffer;

	public void matrix(Matrix4 m) {
		utilAllowed();
		if (type == GL20.GL_FLOAT_MAT4)
			GL20.glUniformMatrix4(location, false, m.getAccessor());
		else if (type == GL20.GL_FLOAT_MAT3) {
			if (mat4TmpBuffer == null)
				mat4TmpBuffer = new float[16];
			if (mat3TmpBuffer == null)
				mat3TmpBuffer = BufferUtils.createFloatBuffer(9);
			FloatBuffer access = m.getAccessor();
			access.get(mat4TmpBuffer);
			mat3TmpBuffer.position(0);
			mat3TmpBuffer.put(mat4TmpBuffer, 0, 3);
			mat3TmpBuffer.put(mat4TmpBuffer, 4, 3);
			mat3TmpBuffer.put(mat4TmpBuffer, 8, 3);
			mat3TmpBuffer.position(0);
			GL20.glUniformMatrix3(location, false, mat3TmpBuffer);
		} else
			typeMismatch("float matrix4");
	}

	public void color(BufferColor c) {
		ByteBuffer buff = c.getAccessor();
		if (type == GL20.GL_FLOAT_VEC3)
			vector((buff.get(0) & 0xFF) / 255f, (buff.get(1) & 0xFF) / 255f,
					(buff.get(2) & 0xFF) / 255f);
		else
			vector((buff.get(0) & 0xFF) / 255f, (buff.get(1) & 0xFF) / 255f,
					(buff.get(2) & 0xFF) / 255f, (buff.get(3)) / 255f);
	}

	public int location() {
		return location;
	}
}
