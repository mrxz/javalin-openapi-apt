package me.noeri.atlatl.schema;

import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.parametrization.ResolvedTypeParametersMap;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

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
				Schema<?> propertySchema = registry.getSchemaOrReferenceFor(fieldType);
				schema = (ObjectSchema) schema.addProperties(field.getName(), propertySchema);
			}
			return schema;
		}
		return null;
	}

}
