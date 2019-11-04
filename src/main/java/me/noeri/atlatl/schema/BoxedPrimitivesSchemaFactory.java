package me.noeri.atlatl.schema;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import me.noeri.atlatl.utils.TypeUtils;

public class BoxedPrimitivesSchemaFactory implements SchemaFactory {
	private final Map<ResolvedReferenceTypeDeclaration, Supplier<Schema<?>>> lookup;

	public BoxedPrimitivesSchemaFactory(TypeSolver typeSolver) {
		lookup = new HashMap<>();
		lookup.put(typeSolver.solveType("java.lang.Boolean"), BooleanSchema::new);
		lookup.put(typeSolver.solveType("java.lang.Byte"), NumberSchema::new);
		lookup.put(typeSolver.solveType("java.lang.Character"), () -> new StringSchema().maxLength(1));
		lookup.put(typeSolver.solveType("java.lang.Float"), NumberSchema::new);
		lookup.put(typeSolver.solveType("java.lang.Integer"), NumberSchema::new);
		lookup.put(typeSolver.solveType("java.lang.Long"), NumberSchema::new);
		lookup.put(typeSolver.solveType("java.lang.Short"), NumberSchema::new);
		lookup.put(typeSolver.solveType("java.lang.Double"), NumberSchema::new);
		lookup.put(typeSolver.solveType("java.lang.String"), StringSchema::new);
	}

	@Override
	public Schema<?> createSchema(ResolvedType type, SchemaRegistry registry) {
		return lookup.entrySet().stream()
			.filter(entry -> TypeUtils.isAssignable(type, entry.getKey()))
			.findFirst()
			.map(entry -> entry.getValue().get())
			.orElse(null);
	}

}
