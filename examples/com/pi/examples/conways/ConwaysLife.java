package com.pi.examples.conways;

import org.lwjgl.opengl.GL11;

import com.pi.core.framebuffer.FrameBuffer;
import com.pi.core.glsl.ShaderProgram;
import com.pi.core.model.Model;
import com.pi.core.texture.DataTexture;
import com.pi.core.texture.Texture;
import com.pi.core.texture.TextureWrap;
import com.pi.core.util.DoubleBuffered;
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

	private DoubleBuffered<FrameBuffer> frameBuffers;
	private DoubleBuffered<DataTexture> textures;

	private ShaderProgram conway;
	private ShaderProgram render;

	private Model<SimpleVertex> plane;

	private static ShaderProgram createShader(String name) {
		ShaderProgram p = new ShaderProgram();
		p.vertex(ConwaysLife.class.getResourceAsStream(name + ".vs"));
		p.fragment(ConwaysLife.class.getResourceAsStream(name + ".fs"));
		p.gpuAlloc();
		p.link();
		return p;
	}

	@Override
	public void init() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		super.setSize(W, H);

		textures = new DoubleBuffered<>(new DataTexture(1, W, H),
				new DataTexture(1, W, H));
		textures.getFront().wrap(TextureWrap.REPEAT, TextureWrap.REPEAT)
				.gpuAlloc();
		textures.getBack().wrap(TextureWrap.REPEAT, TextureWrap.REPEAT)
				.gpuAlloc();

		for (Vector[] vv : textures.getBack().vectors)
			for (Vector v : vv)
				v.setV(Math.random() > 0.75 ? 1 : 0);
		textures.getBack().gpuUpload();

		frameBuffers = new DoubleBuffered<>(new FrameBuffer(),
				new FrameBuffer());
		frameBuffers.getFront().attachColor(textures.getFront());
		frameBuffers.getFront().gpuAlloc();

		frameBuffers.getBack().attachColor(textures.getBack());
		frameBuffers.getBack().gpuAlloc();

		conway = createShader("conway");
		conway.bind();
		conway.uniform("viewport").vector(W, H);
		ShaderProgram.unbind();

		render = createShader("render");
		render.bind();
		render.uniform("viewport").vector(W, H);
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

		conway.bind();
		conway.uniform("state").texture(textures.getBack());
		conway.bindSamplers();
		frameBuffers.getFront().bind();
		plane.render();
		FrameBuffer.unbind();
		Texture.unbind();
		ShaderProgram.unbind();

		render.bind();
		render.uniform("state").texture(textures.getFront());
		render.bindSamplers();
		plane.render();
		Texture.unbind();
		ShaderProgram.unbind();

	}

	@Override
	public void update() {
		textures.flip();
		frameBuffers.flip();
	}

	@Override
	public void dispose() {
		frameBuffers.getFront().gpuFree();
		frameBuffers.getBack().gpuFree();
		textures.getFront().gpuFree();
		textures.getBack().gpuFree();
		plane.gpuFree();
		render.gpuFree();
		conway.gpuFree();
	}

	public static void main(String[] args) {
		new ConwaysLife().start();
	}
}
