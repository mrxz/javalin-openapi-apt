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
package me.noeri.atlatl.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import java.io.IOException;
import me.noeri.atlatl.Reporter;
import me.noeri.atlatl.TestBase;
import me.noeri.atlatl.schema.SchemaRegistry;
import me.noeri.atlatl.schema.SimpleModelNamingStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class OperationParserTest extends TestBase {

	private final SchemaRegistry schemaRegistry = new SchemaRegistry(new SimpleModelNamingStrategy(),
			(ResolvedType type, SchemaRegistry registry) -> new StringSchema());
	private final Reporter noopReporter = new Reporter() {
		@Override
		public void report(MessageKind kind, String message, String qualifiedName) {
			// No-op
		}
	};

	@ParameterizedTest
	@ValueSource(strings = {
		"AdditionMethod",
		"EmptyMethod",
		"SimpleMethod",
		"DocumentedMethod",
	})
	public void shouldGenerateCorrectOperation(String testCase) throws IOException {
		ParseResult<CompilationUnit> cu = parser.parse(getResourceAsString("operations/" + testCase + ".java.txt"));
		MethodDeclaration declaration = cu.getResult().get().findFirst(MethodDeclaration.class).get();

		OperationParser operationParser = new OperationParser(typeSolver, schemaRegistry, noopReporter);
		Operation operation = operationParser.analyzeOperation(declaration, declaration.getNameAsString());

		String actual = Yaml.pretty().writeValueAsString(operation);
		String expected = getResourceAsString("operations/" + testCase + ".yaml");

		assertEquals(expected, actual);
	}
}
