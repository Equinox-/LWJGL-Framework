package com.pi.core.glsl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL20;

public class ShaderLogParser {
	private static final Pattern LINE_FINDER_AMD = Pattern.compile("[0-9]+:([0-9]+)");
	private static final Pattern LINE_FINDER_NVIDIA = Pattern.compile("line ([0-9]+), column [0-9]+");

	public static String shaderCompileLog(String[] source, int shader) {
		String log = GL20.glGetShaderInfoLog(shader, 4096);
		String[] logLines = log.split("\n");
		StringBuilder res = new StringBuilder(log.length());
		int maxLen = 0;
		for (String s : logLines)
			maxLen = Math.max(maxLen, s.length());

		for (int i = 0; i < logLines.length; i++) {
			boolean foundLine = false;
			for (Pattern pt : new Pattern[] { LINE_FINDER_AMD, LINE_FINDER_NVIDIA }) {
				Matcher m = pt.matcher(logLines[i]);
				if (m.find()) {
					res.append(logLines[i]);
					for (int r = logLines[i].length(); r < maxLen + 4; r++)
						res.append(' ');
					int ctx;
					try {
						ctx = Integer.parseInt(m.group(1)) - 1;
					} catch (NumberFormatException e) {
						ctx = -1;
					}
					if (ctx >= 0) {
						res.append("Context: ").append(ctx >= 0 && ctx < source.length ? source[ctx].trim() : "Unknown")
								.append('\n');
						foundLine = true;
						break;
					}
				}
			}
			if (!foundLine)
				res.append(logLines[i]).append('\n');
		}
		return res.toString();
	}
}
