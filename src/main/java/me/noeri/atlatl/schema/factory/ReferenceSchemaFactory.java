package me.noeri.atlatl.schema.factory;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.parametrization.ResolvedTypeParametersMap;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Optional;
import me.noeri.atlatl.schema.SchemaRegistry;
import me.noeri.atlatl.utils.TypeUtils;

public class ReferenceSchemaFactory implements SchemaFactory {

	@Override
	public Schema<?> createSchema(ResolvedType type, SchemaRegistry registry) {
		if(type.isReferenceType()) {
			ObjectSchema schema = new ObjectSchema();
			for(ResolvedFieldDeclaration field : type.asReferenceType().getTypeDeclaration().getAllFields()) {
				ResolvedTypeParametersMap typeParametersMap = type.asReferenceType().typeParametersMap();

				ResolvedType fieldType = field.getType();
				if(fieldType.isReferenceType()) {
					if(!fieldType.asReferenceType().typeParametersMap().isEmpty() && !typeParametersMap.isEmpty()) {
						fieldType = type.asReferenceType().useThisTypeParametersOnTheGivenType(fieldType);
					}
					if(fieldType.asReferenceType().getId().equals(type.asReferenceType().getId())) {
						// Recursion
						System.err.println("Recursion in data models not supported!");
						continue;
					}
				}

				// Note: JavaDoc is only on the AST, but we're investigatin the types.
				Optional<String> comment = Optional.ofNullable(TypeUtils.getDeclarationFromResolvedReferenceTypeDeclaration(field.declaringType().asReferenceType()))
					.flatMap(node -> node.findFirst(FieldDeclaration.class, f -> f.getVariable(0).getName().toString().equals(field.getName())))
					.flatMap(FieldDeclaration::getJavadoc)
					.map(Javadoc::toText)
					.map(String::trim);

				Schema<?> propertySchema = registry.getSchemaOrReferenceFor(fieldType);
				// FIXME: This could alter existing schemas.
				propertySchema.description(comment.orElse(null));
				schema = (ObjectSchema) schema.addProperties(field.getName(), propertySchema);
			}
			return schema;
		}
		return null;
	}

}
