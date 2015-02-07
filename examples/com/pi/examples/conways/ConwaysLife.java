package com.pi.examples.conways;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.pi.core.framebuffer.FrameBuffer;
import com.pi.core.glsl.ShaderProgram;
import com.pi.core.model.Model;
import com.pi.core.texture.DataTexture;
import com.pi.core.texture.Texture;
import com.pi.core.texture.TextureWrap;
import com.pi.core.vertex.AttrLayout;
import com.pi.core.vertex.VertexData;
import com.pi.core.wind.GLWindow;
import com.pi.math.vector.Vector;
import com.pi.math.vector.VectorBuff;

public class ConwaysLife extends GLWindow {
	private static final int W = 256, H = 256;

	public static class SimpleVertex {
		@AttrLayout(layout = 0, dimension = 2)
		VectorBuff pos;
	}

	private FrameBuffer frontFB;
	private DataTexture frontTex;

	private FrameBuffer backFB;
	private DataTexture backTex;

	private ShaderProgram conway;
	private ShaderProgram render;

	private Model<SimpleVertex> plane;

	private static ShaderProgram createShader(String name) {
		ShaderProgram p = new ShaderProgram();
		p.vertex(ConwaysLife.class.getResourceAsStream(name + ".vs"));
		p.fragment(ConwaysLife.class.getResourceAsStream(name + ".fs"));
		p.link();
		return p;
	}

	@Override
	public void init() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		super.setSize(W, H);

		frontTex = new DataTexture(1, W, H);
		frontTex.wrap(TextureWrap.REPEAT, TextureWrap.REPEAT);
		frontTex.gpuAlloc();

		frontFB = new FrameBuffer();
		frontFB.attachColor(frontTex);
		frontFB.gpuAlloc();

		backTex = new DataTexture(1, W, H);
		backTex.wrap(TextureWrap.REPEAT, TextureWrap.REPEAT);
		for (Vector[] vv : backTex.vectors)
			for (Vector v : vv)
				v.setV(Math.random() > 0.75 ? 1 : 0);
		backTex.gpuAlloc();
		backTex.gpuUpload();

		backFB = new FrameBuffer();
		backFB.attachColor(backTex);
		backFB.gpuAlloc();

		conway = createShader("conway");
		conway.bind();
		GL20.glUniform1i(conway.uniform("state"), 0);
		GL20.glUniform2f(conway.uniform("viewport"), W, H);
		ShaderProgram.unbind();

		render = createShader("render");
		render.bind();
		GL20.glUniform1i(render.uniform("state"), 0);
		GL20.glUniform2f(render.uniform("viewport"), W, H);
		ShaderProgram.unbind();

		VertexData<SimpleVertex> plVerts = new VertexData<>(SimpleVertex.class,
				4);
		plVerts.vertexDB[0].pos.setV(-1, -1);
		plVerts.vertexDB[1].pos.setV(1, -1);
		plVerts.vertexDB[2].pos.setV(1, 1);
		plVerts.vertexDB[3].pos.setV(-1, 1);
		plane = new Model<>(plVerts, new int[] { 0, 1, 2, 0, 2, 3 },
				GL11.GL_TRIANGLES);
		plane.gpuAlloc();
		plane.gpuUpload();

		GL11.glViewport(0, 0, W, H);
	}

	@Override
	public void render() {
		// Run update
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);

		conway.bind();
		backTex.bind();
		frontFB.bind();
		plane.render();
		FrameBuffer.unbind();
		Texture.unbind();
		ShaderProgram.unbind();

		render.bind();
		frontTex.bind();
		plane.render();
		Texture.unbind();
		ShaderProgram.unbind();

	}

	@Override
	public void update() {
		// Swap buffers
		DataTexture backTexOld = backTex;
		FrameBuffer backFBOld = backFB;
		backTex = frontTex;
		backFB = frontFB;
		frontTex = backTexOld;
		frontFB = backFBOld;
	}

	@Override
	public void dispose() {
		frontFB.gpuFree();
		frontTex.gpuFree();
		backFB.gpuFree();
		backTex.gpuFree();
		plane.gpuFree();
		render.dispose();
		conway.dispose();
	}

	public static void main(String[] args) {
		new ConwaysLife().start();
	}
}
