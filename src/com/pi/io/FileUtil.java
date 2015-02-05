package com.pi.io;

import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
	public static String readStreamFully(InputStream f) throws IOException {
		byte[] buffer = new byte[1024];
		StringBuilder string = new StringBuilder(f.available());
		int len;
		while ((len = f.read(buffer)) > 0) {
			string.append(new String(buffer, 0, len));
		}
		f.close();
		return string.toString();
	}
}
