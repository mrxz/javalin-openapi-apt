package me.noeri.atlatl.schema;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import me.noeri.atlatl.utils.TypeUtils;

public class BoxedPrimitivesSchemaFactory implements SchemaFactory {
	private final ResolvedReferenceTypeDeclaration stringType;
	private final ResolvedReferenceTypeDeclaration booleanType;
	private final ResolvedReferenceTypeDeclaration longType;

	public BoxedPrimitivesSchemaFactory(TypeSolver typeSolver) {
		this.stringType = typeSolver.solveType("java.lang.String");
		this.booleanType = typeSolver.solveType("java.lang.Boolean");
		this.longType = typeSolver.solveType("java.lang.Long");
	}

	@Override
	public Schema createSchema(ResolvedType type, SchemaRegistry registry) {
		if(type.isReferenceType()) {
			if(TypeUtils.isAssignable(type, stringType)) {
				return new StringSchema();
			}
			if(TypeUtils.isAssignable(type, booleanType)) {
				return new BooleanSchema();
			}
			if(TypeUtils.isAssignable(type, longType)) {
				return new NumberSchema();
			}
		}
		return null;
	}

}
