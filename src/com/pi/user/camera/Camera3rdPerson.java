package com.pi.user.camera;

import org.lwjgl.glfw.GLFW;

import com.pi.core.wind.GLWindow;
import com.pi.math.matrix.Matrix4;
import com.pi.math.matrix.SpecialMatrix;
import com.pi.math.vector.VectorBuff3;

public class Camera3rdPerson implements Camera {
	private final Matrix4 rotMatrix = new Matrix4().makeIdentity();
	private final Matrix4 view = new Matrix4();
	private final GLWindow window;

	private float x, y, z;
	public float pitch, yaw, offset;
	private long lastMoveProc;

	// rad/s
	private float yawRate = 4;
	private float pitchRate = 4;
	private float offsetRate = 10f;
	private float moveRate = 10f;

	public Camera3rdPerson(GLWindow window, float offset) {
		this(window, 0, 0, offset);
	}

	public Camera3rdPerson(GLWindow window, float yaw, float pitch, float offset) {
		this.window = window;
		this.yaw = yaw;
		this.pitch = pitch;
		this.offset = offset;
		this.lastMoveProc = -1;
	}

	public Camera3rdPerson center(float x, float y, float z) {
		this.x = -x;
		this.y = -y;
		this.z = -z;
		return this;
	}

	public float centerX() {
		return -x;
	}

	public float centerY() {
		return -y;
	}

	public float centerZ() {
		return -z;
	}

	public float getOffset() {
		return offset;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public Camera3rdPerson position(float yaw, float pitch, float offset) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.offset = offset;
		return this;
	}

	@Override
	public VectorBuff3 position(VectorBuff3 tmp) {
		float csB = (float) Math.cos(pitch) * -offset;
		tmp.setV(csB * (float) Math.sin(yaw) - x, (float) Math.sin(pitch) * offset - y,
				-csB * (float) Math.cos(yaw) - z);
		return tmp;
	}

	public Camera3rdPerson rates(float yawRate, float pitchRate, float offsetRate) {
		return rates(yawRate, pitchRate, offsetRate, moveRate);
	}

	public Camera3rdPerson rates(float yawRate, float pitchRate, float offsetRate, float moveRate) {
		this.yawRate = yawRate;
		this.pitchRate = pitchRate;
		this.offsetRate = offsetRate;
		this.moveRate = moveRate;
		return this;
	}

	@Override
	public String toString() {
		return "Camera3rdPerson[yaw=" + Math.toDegrees(yaw) + ",pitch=" + Math.toDegrees(pitch) + ",offset=" + offset
				+ "]";
	}

	@Override
	public void update() {
		if (lastMoveProc >= 0) {
			final long passedMS = System.currentTimeMillis() - lastMoveProc;
			final float passed = passedMS / 1000.0f;
			if (window.getEvents().isKeyDown(GLFW.GLFW_KEY_A))
				yaw += passed * yawRate;
			else if (window.getEvents().isKeyDown(GLFW.GLFW_KEY_D))
				yaw -= passed * yawRate;

			if (window.getEvents().isKeyDown(GLFW.GLFW_KEY_W))
				pitch += passed * pitchRate;
			else if (window.getEvents().isKeyDown(GLFW.GLFW_KEY_S))
				pitch -= passed * pitchRate;

			if (window.getEvents().isKeyDown(GLFW.GLFW_KEY_Q))
				offset -= passed * offsetRate;
			else if (window.getEvents().isKeyDown(GLFW.GLFW_KEY_E))
				offset += passed * offsetRate;

			if (window.getEvents().isKeyDown(GLFW.GLFW_KEY_UP))
				y -= passed * moveRate;
			else if (window.getEvents().isKeyDown(GLFW.GLFW_KEY_DOWN))
				y += passed * moveRate;

			if (window.getEvents().isKeyDown(GLFW.GLFW_KEY_LEFT)) {
				x += Math.cos(yaw) * passed * moveRate;
				z += Math.sin(yaw) * passed * moveRate;
			} else if (window.getEvents().isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
				x -= Math.cos(yaw) * passed * moveRate;
				z -= Math.sin(yaw) * passed * moveRate;
			}

			lastMoveProc += passedMS;
		} else {
			lastMoveProc = System.currentTimeMillis();
		}
	}

	@Override
	public Matrix4 view() {
		view.setTranslation(0, -offset, 0);

		// rotMatrix.makeIdentity().setAxisAngle(pitch, 1, 0, 0);
		SpecialMatrix.angleX(rotMatrix.makeIdentity(), pitch);
		rotMatrix.preMul(view);

		// matrix.makeIdentity().setAxisAngle(yaw, 0, 0, 1);
		SpecialMatrix.angleZ(view.makeIdentity(), yaw);
		view.preMul(rotMatrix);
		view.preMultiplyTransform(x, y, z);
		
		return view;
	}
}
