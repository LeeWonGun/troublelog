package com.min.edu.file;

import java.util.Set;

public final class FileConstants {
	
	private FileConstants() {}
	
	public static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
	public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;  // 5MB

}
