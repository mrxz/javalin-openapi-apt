package me.noeri.atlatl.schema;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import me.noeri.atlatl.utils.TypeUtils;

public class MapSchemaFactory implements SchemaFactory {
	private final ResolvedReferenceTypeDeclaration mapType;

	public MapSchemaFactory(TypeSolver typeSolver) {
		this.mapType = typeSolver.solveType("java.util.Map").asReferenceType();
	}

	@Override
	public Schema<?> createSchema(ResolvedType type, SchemaRegistry registry) {
		if(TypeUtils.isAssignable(type, mapType)) {
			ResolvedType elementType = type.asReferenceType().getTypeParametersMap().get(1).b;
			Schema<?> elementSchema = registry.getSchemaOrReferenceFor(elementType);
			return new ObjectSchema().additionalProperties(elementSchema);
		}
		return null;
	}

}
