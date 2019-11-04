package me.noeri.atlatl.schema;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import me.noeri.atlatl.utils.TypeUtils;

public class CollectionSchemaFactory implements SchemaFactory {
	private final ResolvedReferenceTypeDeclaration collectionType;

	public CollectionSchemaFactory(TypeSolver typeSolver) {
		this.collectionType = typeSolver.solveType("java.util.Collection").asReferenceType();
	}

	@Override
	public Schema<?> createSchema(ResolvedType type, SchemaRegistry registry) {
		if(TypeUtils.isAssignable(type, collectionType)) {
			ResolvedType elementType = type.asReferenceType().typeParametersValues().get(0);
			Schema<?> elementSchema = registry.getSchemaOrReferenceFor(elementType);
			return new ArraySchema().items(elementSchema);
		}
		return null;
	}

}
