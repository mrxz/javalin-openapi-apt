package me.noeri.atlatl.utils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtils {

	public static String qualifiedNameToFile(String qualifiedName) {
		return Stream.of(qualifiedName.split("\\."))
				.collect(Collectors.joining("/", "", ".java"));
	}

	public static String fileToQualifiedName(String file) {
		return Stream.of(file.replaceFirst("\\.java$", "").split("/"))
				.collect(Collectors.joining("."));
	}
}
