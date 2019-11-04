package me.noeri.atlatl.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FileUtilsTest {

	@ParameterizedTest
	@MethodSource("provideQualifiedNameAndFilePairs")
	public void shouldProperlyConvertQualifiedNameToFile(String qualifiedName, String expected) {
		String actual = FileUtils.qualifiedNameToFile(qualifiedName);

		assertEquals(expected, actual);
	}

	@ParameterizedTest
	@MethodSource("provideQualifiedNameAndFilePairs")
	public void shouldProperlyConvertFileToQualifiedName(String expected, String file) {
		String actual = FileUtils.fileToQualifiedName(file);

		assertEquals(expected, actual);
	}

	private static Stream<Arguments> provideQualifiedNameAndFilePairs() {
	    return Stream.of(
	      Arguments.of("org.example.ClassName", "org/example/ClassName.java"),
	      Arguments.of("ClassName", "ClassName.java"),
	      Arguments.of("a.b.c.d.e.f.g", "a/b/c/d/e/f/g.java")
	    );
	}
}
