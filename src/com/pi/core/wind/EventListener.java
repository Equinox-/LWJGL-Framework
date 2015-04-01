package com.pi.core.wind;

public interface EventListener {
	// Return true if this event has been consumed, and should no longer be processed.
	public default boolean mousePressed(int button, float x, float y,
			int modifiers) {
		return false;
	}

	public default boolean mouseReleased(int button, float x, float y,
			int modifiers) {
		return false;
	}

	public default boolean mouseMoved(float x, float y, int modifiers) {
		return false;
	}

	public default boolean charTyped(int unicodeCodepoint) {
		return false;
	}

	public default boolean keyPressed(int key, int modifiers) {
		return false;
	}

	public default boolean keyReleased(int key, int modifiers) {
		return false;
	}

	public default boolean scrollChanged(float dx, float dy) {
		return false;
	}
}
