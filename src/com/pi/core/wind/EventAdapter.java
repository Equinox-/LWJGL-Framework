package com.pi.core.wind;

public abstract class EventAdapter implements EventListener {
	@Override
	public boolean charTyped(long time, int unicodeCodepoint) {
		return false;
	}

	@Override
	public boolean keyPressed(long time, int key, int modifiers) {
		return false;
	}

	@Override
	public boolean keyReleased(long time, int key, int modifiers) {
		return false;
	}

	@Override
	public boolean mouseMoved(long time, float x, float y, int modifiers) {
		return false;
	}

	// Return true if this event has been consumed, and should no longer be processed.
	@Override
	public boolean mousePressed(long time, int button, float x, float y, int modifiers) {
		return false;
	}

	@Override
	public boolean mouseReleased(long time, int button, float x, float y, int modifiers) {
		return false;
	}

	@Override
	public boolean scrollChanged(long time, float dx, float dy) {
		return false;
	}

	@Override
	public boolean sizeChanged(long time, float width, float height) {
		return false;
	}
}
