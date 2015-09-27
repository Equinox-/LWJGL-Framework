package com.pi.core.wind;

public interface EventListener {
	// Return true if this event has been consumed, and should no longer be processed.
	public boolean mousePressed(long time, int button, float x, float y, int modifiers);

	public boolean mouseReleased(long time, int button, float x, float y, int modifiers);

	public boolean mouseMoved(long time, float x, float y, int modifiers);

	public boolean charTyped(long time, int unicodeCodepoint);

	public boolean keyPressed(long time, int key, int modifiers);

	public boolean keyReleased(long time, int key, int modifiers);

	public boolean scrollChanged(long time, float dx, float dy);
	
	public boolean sizeChanged(long time, float width, float height);
}
