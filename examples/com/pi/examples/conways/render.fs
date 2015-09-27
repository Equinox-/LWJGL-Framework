#version 330 core

uniform sampler2D state;
uniform vec2 viewport;

out vec3 fragColor;

void main() {
	fragColor = texture2D(state, gl_FragCoord.xy / viewport).xxx;
}
