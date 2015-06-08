package com.pi.core.wind;

public interface EventListener {
	// Return true if this event has been consumed, and should no longer be processed.
	public boolean mousePressed(int button, float x, float y, int modifiers);

	public boolean mouseReleased(int button, float x, float y, int modifiers);

	public boolean mouseMoved(float x, float y, int modifiers);

	public boolean charTyped(int unicodeCodepoint);

	public boolean keyPressed(int key, int modifiers);

	public boolean keyReleased(int key, int modifiers);

	public boolean scrollChanged(float dx, float dy);
	
	public boolean sizeChanged(float width, float height);
}
