package com.pi.core.glsl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.pi.core.texture.Texture;
import com.pi.core.util.Bindable;
import com.pi.core.util.GLIdentifiable;
import com.pi.core.util.GPUObject;
import com.pi.io.FileUtil;

public class ShaderProgram extends GPUObject<ShaderProgram> implements
		Bindable, GLIdentifiable {
	static final int MAX_TEXTURE_UNITS = 16;
	private static ShaderProgram currentShader;

	private int programID;
	private final List<Integer> attachedObjects;

	private final Map<String, ShaderUniform> uniformsByName;
	private ShaderUniform[] uniformsByID;
	private final Map<String, ShaderUniformBlock> uniformBlocksByName;
	private ShaderUniformBlock[] uniformBlocksByID;

	private final static Map<String, Integer> SHADER_TYPE_MAP;
	static {
		SHADER_TYPE_MAP = new HashMap<>();
		SHADER_TYPE_MAP.put("GL_VERTEX_SHADER", GL20.GL_VERTEX_SHADER);
		SHADER_TYPE_MAP.put("GL_TESS_CONTROL_SHADER",
				GL40.GL_TESS_CONTROL_SHADER);
		SHADER_TYPE_MAP.put("GL_TESS_EVALUATION_SHADER",
				GL40.GL_TESS_EVALUATION_SHADER);
		SHADER_TYPE_MAP.put("GL_GEOMETRY_SHADER", GL32.GL_GEOMETRY_SHADER);
		SHADER_TYPE_MAP.put("GL_FRAGMENT_SHADER", GL20.GL_FRAGMENT_SHADER);
		SHADER_TYPE_MAP.put("GL_COMPUTE_SHADER", GL43.GL_COMPUTE_SHADER);
	}

	// Needs to be accessed by ShaderUniform; therefore not private
	public Texture[] textureUnit;
	public int[] textureUnitRefCount;
	// Limits rebinding
	private static final Texture[] ACTIVE_TEXTURE_UNITS = new Texture[MAX_TEXTURE_UNITS];

	public ShaderProgram() {
		this.uniformsByName = new HashMap<>();
		this.uniformBlocksByName = new HashMap<>();
		this.attachedObjects = new ArrayList<>(2);
		this.programID = -1;
		this.textureUnit = new Texture[MAX_TEXTURE_UNITS];
		this.textureUnitRefCount = new int[MAX_TEXTURE_UNITS];
	}

	@Override
	protected void gpuAllocInternal() {
		if (this.programID >= 0)
			gpuFreeInternal();
		this.programID = GL20.glCreateProgram();
	}

	private static final Pattern LINE_FINDER_AMD = Pattern
			.compile("[0-9]+:([0-9]+)");
	private static final Pattern LINE_FINDER_NVIDIA = Pattern
			.compile("line ([0-9]+), column [0-9]+");

	private static int compileShader(String src, int type)
			throws InstantiationException {
		src = ShaderPreprocessor.preprocess(src);
		String[] lines = src.split("\n");

		int shader = GL20.glCreateShader(type);
		GL20.glShaderSource(shader, src);
		GL20.glCompileShader(shader);
		if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			String log = GL20.glGetShaderInfoLog(shader, 4096);
			GL20.glDeleteShader(shader);
			String[] logLines = log.split("\n");
			StringBuilder res = new StringBuilder(log.length());
			int maxLen = 0;
			for (String s : logLines)
				maxLen = Math.max(maxLen, s.length());

			for (int i = 0; i < logLines.length; i++) {
				boolean foundLine = false;
				for (Pattern pt : new Pattern[] { LINE_FINDER_AMD,
						LINE_FINDER_NVIDIA }) {
					Matcher m = pt.matcher(logLines[i]);
					if (m.find()) {
						res.append(logLines[i]);
						for (int r = logLines[i].length(); r < maxLen + 4; r++)
							res.append(' ');
						int ctx;
						try {
							ctx = Integer.parseInt(m.group(1)) - 1;
						} catch (NumberFormatException e) {
							ctx = -1;
						}
						if (ctx >= 0) {
							res.append("Context: ")
									.append(ctx >= 0 && ctx < lines.length ? lines[ctx]
											.trim() : "Unknown").append('\n');
							foundLine = true;
							break;
						}
					}
					break;
				}
				if (!foundLine)
					res.append(logLines[i]).append('\n');
			}
			System.err.println("Shader compile failure: \n" + res.toString());
			throw new InstantiationException();
		}
		return shader;
	}

	public ShaderProgram attach(int type, InputStream src) {
		try {
			attachedObjects.add(compileShader(FileUtil.readStreamFully(src),
					type));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public ShaderProgram fragment(InputStream src) {
		return attach(GL20.GL_FRAGMENT_SHADER, src);
	}

	public ShaderProgram vertex(InputStream src) {
		return attach(GL20.GL_VERTEX_SHADER, src);
	}

	public ShaderProgram joined(InputStream src) {
		try {
			String[] lines = FileUtil.readStreamFully(src).split("(\r|)\n");
			StringBuilder tmp = new StringBuilder();
			String type = null;
			int typeV = 0;
			for (String line : lines) {
				if (line.trim().startsWith("//")) {
					String ttype = line.trim().substring(2).trim()
							.toUpperCase();
					Integer rt = SHADER_TYPE_MAP.get(ttype);
					if (rt != null && tmp.length() > 0 && type != null) {
						attachedObjects
								.add(compileShader(tmp.toString(), typeV));
						tmp.setLength(0);
					}
					if (rt != null) {
						type = ttype;
						typeV = rt;
					}
				} else
					tmp.append(line).append('\n');
			}
			if (tmp.length() > 0 && type != null) {
				attachedObjects.add(compileShader(tmp.toString(), typeV));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public ShaderProgram link() {
		if (programID < 0)
			gpuAllocInternal();
		for (int obj : attachedObjects)
			GL20.glAttachShader(programID, obj);
		GL20.glLinkProgram(programID);
		if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			System.err.println("Shader program wasn't linked correctly.");
			System.err.println(GL20.glGetProgramInfoLog(programID, 1024));
			throw new RuntimeException();
		}
		loadUniforms();
		return this;
	}

	@Override
	protected void gpuFreeInternal() {
		unbind();
		for (int obj : attachedObjects) {
			GL20.glDetachShader(programID, obj);
			GL20.glDeleteShader(obj);
		}
		GL20.glDeleteProgram(programID);
		programID = -1;
	}

	public boolean using() {
		return currentShader == this;
	}

	@Override
	public int getID() {
		return programID;
	}

	@Override
	public void bind() {
		if (programID == -1)
			throw new RuntimeException("Attempted to bind a disposed shader.");
		if (currentShader == this)
			return;
		GL20.glUseProgram(programID);
		currentShader = this;
	}

	public static void unbind() {
		GL20.glUseProgram(0);
		currentShader = null;
	}

	public static ShaderProgram current() {
		return currentShader;
	}

	private void loadUniforms() {
		int uniformBlockCount = GL20.glGetProgrami(programID,
				GL31.GL_ACTIVE_UNIFORM_BLOCKS);
		uniformBlocksByID = new ShaderUniformBlock[uniformBlockCount];
		uniformBlocksByName.clear();
		for (int i = 0; i < uniformBlockCount; i++) {
			String name = GL31.glGetActiveUniformBlockName(programID, i);
			uniformBlocksByName
					.put(name, uniformBlocksByID[i] = new ShaderUniformBlock(
							this, i, name));
		}

		int uniformCount = GL20.glGetProgrami(programID,
				GL20.GL_ACTIVE_UNIFORMS);
		int uniformMaxNameLength = GL20.glGetProgrami(programID,
				GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH);
		uniformsByID = new ShaderUniform[uniformCount];
		uniformsByName.clear();

		for (int i = 0; i < uniformCount; i++) {
			uniformsByID[i] = new ShaderUniform(this, i, uniformMaxNameLength);
			uniformsByName.put(uniformsByID[i].name(), uniformsByID[i]);
		}
	}

	public ShaderUniformBlock uniformBlock(int id) {
		return uniformBlocksByID[id];
	}

	public ShaderUniformBlock uniformBlock(String name) {
		return uniformBlocksByName.get(name);
	}

	public ShaderUniform uniform(int id) {
		return uniformsByID[id];
	}

	public ShaderUniform uniform(String name) {
		ShaderUniform v = uniformsByName.get(name);
//		if (v == null)
//			System.err.println("Tried to query shader for invalid uniform: "
//					+ name);
		return v;
	}

	public void commitData() {
		for (int i = 0; i < textureUnit.length; i++) {
			if (ACTIVE_TEXTURE_UNITS[i] != textureUnit[i]) {
				Texture.glActiveTexture(i);
				if (textureUnit[i] != null)
					textureUnit[i].bind();
				else
					Texture.unbind();
				ACTIVE_TEXTURE_UNITS[i] = textureUnit[i];
			}
		}
		for (ShaderUniformBlock block : uniformBlocksByID) {
			block.uploadIfNeeded();
		}
	}

	@Override
	protected ShaderProgram me() {
		return this;
	}
}
