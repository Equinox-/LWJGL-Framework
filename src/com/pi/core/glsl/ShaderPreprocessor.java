package com.pi.core.glsl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pi.io.FileUtil;

public class ShaderPreprocessor {
	private final static Map<String, String> SHADER_INCLUDE_MAP = new HashMap<>();
	private final static Map<String, String> PREDEFINES_MAP = new HashMap<>();

	public static void deregisterInclude(String name) {
		SHADER_INCLUDE_MAP.remove(name);
	}

	public static void registerInclude(String name, String src) {
		SHADER_INCLUDE_MAP.put(name, src);
	}

	public static void define(String name, String value) {
		if (value == null)
			PREDEFINES_MAP.remove(name.trim());
		else
			PREDEFINES_MAP.put(name.trim(), value.trim());
	}

	public static void registerInclude(String name, InputStream src) {
		try {
			registerInclude(name, FileUtil.readStreamFully(src));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final Pattern INCLUDE = Pattern
			.compile("#include[ \t]+?([^ \t\n\r]+)");
	private static final Pattern FOREACH = Pattern
			.compile("#foreach[ \t]*\\([ \t]*([^,\n]*)[ \t]*,[ \t]*([^,\n]*)[ \t]*,[ \t]*([^,\n]*)[ \t]*\\)[ \t]*");
	private static final Pattern VERSION = Pattern
			.compile("#version[^\n\r]*(\r|)\n");

	public static String preprocess(String src) {
		return doForeach(doIncludes(doDefines(src), new HashSet<String>()));
	}

	private static String doDefines(String src) {
		Matcher mt = VERSION.matcher(src);
		boolean np = mt.find();
		StringBuilder res = new StringBuilder(src.length());
		if (np)
			res.append(src, 0, mt.end());
		for (Entry<String, String> define : PREDEFINES_MAP.entrySet()) {
			res.append("#define ").append(define.getKey()).append(' ')
					.append(define.getValue()).append('\n');

		}
		if (np)
			res.append(src, mt.end(), src.length());
		else
			res.append(src);
		return res.toString();
	}

	private static String doIncludes(String src, Set<String> hasIncluded) {
		Matcher mt = INCLUDE.matcher(src);
		boolean np = mt.find();
		if (np) {
			StringBuilder res = new StringBuilder(src.length());
			int prevHead = 0;
			do {
				res.append(src, prevHead, mt.start());
				if (hasIncluded.add(mt.group(1))) {
					String included = SHADER_INCLUDE_MAP.get(mt.group(1));
					if (included != null)
						res.append(included);
					else
						System.err.println("Unable to include: " + mt.group(1)
								+ " (It isn't defined)");
				}
				prevHead = mt.end();
			} while (mt.find());
			res.append(src, prevHead, src.length());
			return doIncludes(res.toString(), hasIncluded);
		} else
			return src;
	}

	private static int evalMath(String code) {
		for (Entry<String, String> entry : PREDEFINES_MAP.entrySet())
			code = code.replace(entry.getKey(), entry.getValue());
		code = code.replaceAll("[()]", "").trim();
		return Integer.parseInt(code);
	}

	private static String doForeach(String src) {
		Matcher mt = FOREACH.matcher(src);
		boolean np = mt.find();
		if (np) {
			StringBuilder res = new StringBuilder(src.length());
			int prevHead = 0;
			do {
				res.append(src, prevHead, mt.start());
				String func = mt.group(1);
				String start = mt.group(2);
				String end = mt.group(3);
				for (int i = evalMath(start); i < evalMath(end); i++) {
					res.append(func).append('(').append(i).append(");");
				}
				prevHead = mt.end();
			} while (mt.find());
			res.append(src, prevHead, src.length());
			return res.toString();
		} else
			return src;
	}
}
