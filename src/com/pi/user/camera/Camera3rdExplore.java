package com.pi.user.camera;

import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import com.pi.core.wind.GLWindow;
import com.pi.math.matrix.Matrix4;
import com.pi.math.vector.Vector;
import com.pi.math.vector.VectorND;

public class Camera3rdExplore implements Camera {
	private final Matrix4 tmpMatrix = new Matrix4().makeIdentity();
	private final DoubleBuffer xPos, yPos;
	private final GLWindow window;

	private Vector prevCamPos;
	private Vector orbitStartPos;
	private double prevScroll;
	private float offset;
	private Matrix4 pose;

	private float scrollOffsetRate = .1f;
	private float panRate = .01f;
	private float fineZoomRate = 0.01f;

	public Camera3rdExplore(GLWindow window, float initialDepth) {
		this.window = window;
		this.offset = -initialDepth;
		this.pose = new Matrix4().makeIdentity();
		this.xPos = BufferUtils.createDoubleBuffer(1);
		this.yPos = BufferUtils.createDoubleBuffer(1);
	}

	@Override
	public void update() {
		double prevX = xPos.get(0);
		double prevY = yPos.get(0);
		GLFW.glfwGetCursorPos(window.getWindowID(), xPos, yPos);

		final float deltaX = (float) (xPos.get(0) - prevX);
		final float deltaY = (float) (yPos.get(0) - prevY);

		double currScroll = window.getEvents().getScrollY();
		offset += (currScroll - prevScroll) * scrollOffsetRate;
		prevScroll = currScroll;

		Vector camPos = new VectorND((float) xPos.get(0)
				/ window.getEvents().getWidth() - 0.5f, (float) yPos.get(0)
				/ window.getEvents().getHeight() - 0.5f, 0);

		Vector orbitStartCache = orbitStartPos;
		orbitStartPos = null;

		if (GLFW.glfwGetKey(window.getWindowID(), GLFW.GLFW_KEY_F4) == GLFW.GLFW_PRESS
				|| (GLFW.glfwGetMouseButton(window.getWindowID(), 2) == GLFW.GLFW_PRESS && GLFW
						.glfwGetKey(window.getWindowID(),
								GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS)
				|| GLFW.glfwGetMouseButton(window.getWindowID(), 1) == GLFW.GLFW_PRESS) {
			if (orbitStartCache == null)
				orbitStartCache = camPos;
			orbitStartPos = orbitStartCache;
			if (orbitStartPos.mag2() > 0.4f * 0.4f) {
				float magA = camPos.magnitude();
				float magB = prevCamPos.magnitude();
				if (magA > 0.1 && magB > 0.1) {
					float dir = -Math
							.signum((camPos.get(0) - prevCamPos.get(0))
									* camPos.get(1));
					float angle = camPos.angle(prevCamPos);
					// Rotating
					if (!Float.isNaN(angle) && !Float.isInfinite(angle))
						pose = Matrix4.multiply(pose, tmpMatrix.makeIdentity()
								.setAxisAngle(dir * angle, 0, 0, 1));
				}
			} else {
				// orbit
				VectorND axis = new VectorND(-deltaY, deltaX, 0);
				float mag = axis.magnitude();
				axis.multiply(1.0f / mag);

				if (mag > 0)
					pose.multiplyInto(tmpMatrix.makeIdentity().setAxisAngle(
							mag / 180.0f, axis));
			}
		} else if (GLFW.glfwGetKey(window.getWindowID(), GLFW.GLFW_KEY_F3) == GLFW.GLFW_PRESS) {
			// fine zoom
			offset += deltaY * fineZoomRate;
		} else if (GLFW.glfwGetKey(window.getWindowID(), GLFW.GLFW_KEY_F2) == GLFW.GLFW_PRESS
				|| GLFW.glfwGetMouseButton(window.getWindowID(), 2) == GLFW.GLFW_PRESS) {
			// Move
			if (deltaX != 0 || deltaY != 0)
				pose.multiplyInto(tmpMatrix.makeIdentity().setTranslation(
						deltaX * panRate * (Math.abs(offset) / 5.0f),
						-deltaY * panRate * (Math.abs(offset) / 5.0f), 0));
		}
		prevCamPos = camPos;
	}

	@Override
	public Matrix4 apply(Matrix4 matrix) {
		matrix.preMultiplyTransform(0, 0, offset);
		Matrix4.multiplyInto(tmpMatrix, pose, matrix);
		return tmpMatrix.copyTo(matrix);
	}

	@Override
	public Vector position() {
		throw new UnsupportedOperationException("Needs work");
		// return new VectorND(-pose.get(12), -pose.get(13), -pose.get(14)
		// - offset);
	}
}
