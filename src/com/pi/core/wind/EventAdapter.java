package com.pi.core.wind;

public abstract class EventAdapter implements EventListener {
	// Return true if this event has been consumed, and should no longer be processed.
	@Override
	public boolean mousePressed(int button, float x, float y, int modifiers) {
		return false;
	}

	@Override
	public boolean mouseReleased(int button, float x, float y, int modifiers) {
		return false;
	}

	@Override
	public boolean mouseMoved(float x, float y, int modifiers) {
		return false;
	}

	@Override
	public boolean charTyped(int unicodeCodepoint) {
		return false;
	}

	@Override
	public boolean keyPressed(int key, int modifiers) {
		return false;
	}

	@Override
	public boolean keyReleased(int key, int modifiers) {
		return false;
	}

	@Override
	public boolean scrollChanged(float dx, float dy) {
		return false;
	}

	@Override
	public boolean sizeChanged(float width, float height) {
		return false;
	}
}
