package com.pi.core.wind;

@SuppressWarnings("static-method")
public abstract class EventListener {
	// Return true if this event has been consumed, and should no longer be processed.
	public boolean mousePressed(int button, float x, float y, int modifiers) {
		return false;
	}

	public boolean mouseReleased(int button, float x, float y, int modifiers) {
		return false;
	}

	public boolean mouseMoved(float x, float y, int modifiers) {
		return false;
	}

	public boolean charTyped(int unicodeCodepoint) {
		return false;
	}

	public boolean keyPressed(int key, int modifiers) {
		return false;
	}

	public boolean keyReleased(int key, int modifiers) {
		return false;
	}

	public boolean scrollChanged(float dx, float dy) {
		return false;
	}
	
	public boolean sizeChanged(float width, float height) {
		return false;
	}
}
