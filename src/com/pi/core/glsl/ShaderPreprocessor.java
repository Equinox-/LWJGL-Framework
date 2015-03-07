package com.pi.core.glsl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pi.io.FileUtil;

public class ShaderPreprocessor {
	private final static Map<String, String> SHADER_INCLUDE_MAP = new HashMap<>();

	public static void registerInclude(String name, String src) {
		SHADER_INCLUDE_MAP.put(name, src);
	}

	public static void registerInclude(String name, InputStream src) {
		try {
			registerInclude(name, FileUtil.readStreamFully(src));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final Pattern include = Pattern
			.compile("#include[ \t]+?([^ \t\n\r]+)");

	public static String preprocess(String src) {
		Matcher mt = include.matcher(src);
		boolean np = mt.find();
		if (np) {
			StringBuilder res = new StringBuilder(src.length());
			int prevHead = 0;
			do {
				res.append(src, prevHead, mt.start());
				String included = SHADER_INCLUDE_MAP.get(mt.group(1));
				if (included != null)
					res.append(included);
				else
					System.err.println("Unable to include: " + mt.group(1)
							+ " (It isn't defined)");
				prevHead = mt.end();
			} while (mt.find());
			res.append(src, prevHead, src.length());
			return preprocess(res.toString());
		} else
			return src;
	}
}
