#version 330 core

uniform sampler2D state;
uniform vec2 viewport;

out float fragColor;

bool stateAt(int x, int y) {
	return texture2D(state, (gl_FragCoord.xy + vec2(x, y)) / viewport).x > 0;
}

void main() {
	bool live = stateAt(0, 0);
	int xo, yo;
	int neighbors = 0;
	for (xo = -1; xo <= 1; xo++)
		for (yo = -1; yo <= 1; yo++)
			if ((xo != 0 || yo != 0) && stateAt(xo, yo))
				++neighbors;
	fragColor = (neighbors == 3 || (neighbors == 2 && live)) ? 1 : 0;
}
