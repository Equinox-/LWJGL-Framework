package com.pi.io;

import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
	public static String hexDump(byte[] data, int off, int len) {
		StringBuilder res = new StringBuilder();
		int nl = 0;
		int space = 0;
		for (int i = off; i < off + len; i++) {
			if (space == 0 && nl == 0)
				res.append(i + "\t");
			if ((data[i] & 0xF0) == 0)
				res.append('0');
			res.append(Integer.toString(data[i] & 0xFF, 16));
			space++;
			if (space >= 2) {
				space = 0;
				res.append(' ');
				nl++;
			}
			if (nl >= 10) {
				res.append('\n');
				nl = 0;
			}
		}
		return res.toString();
	}

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
