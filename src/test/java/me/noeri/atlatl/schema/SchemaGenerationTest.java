package me.noeri.atlatl.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.media.Schema;
import java.io.IOException;
import me.noeri.atlatl.TestBase;
import me.noeri.atlatl.schema.factory.SchemaRegistryFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SchemaGenerationTest extends TestBase {

	@ParameterizedTest
	@ValueSource(strings = {
		"SimpleModel",
		"NumericProperties",
		"BoxedPrimitives",
		"Collections",
		"Enum",
	})
	public void shouldGenerateCorrectSchema(String testCase) throws IOException {
		ParseResult<CompilationUnit> cu = parser.parse(getResource("schemas/" + testCase + ".java.txt"));
		ClassOrInterfaceDeclaration declaration = cu.getResult().get().findFirst(ClassOrInterfaceDeclaration.class).get();
		ResolvedType resolvedType = new ReferenceTypeImpl(declaration.resolve(), typeSolver);

		SchemaRegistry schemaRegistry = SchemaRegistryFactory.createDefaultRegistry(typeSolver);
		Schema<?> schema = schemaRegistry.getSchemaFor(resolvedType);

		String actual = Yaml.pretty().writeValueAsString(schema);
		String expected = getResourceAsString("schemas/" + testCase + ".schema.yaml");

		assertEquals(expected, actual);
	}

}
