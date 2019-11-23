/*
 * Copyright (c) 2019 Noeri Huisman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
