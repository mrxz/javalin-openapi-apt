package me.noeri.atlatl.schema.factory;

import com.github.javaparser.resolution.types.ResolvedType;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import me.noeri.atlatl.schema.SchemaRegistry;

public class ArraySchemaFactory implements SchemaFactory {

	@Override
	public Schema<?> createSchema(ResolvedType type, SchemaRegistry registry) {
		if(type.isArray()) {
			ResolvedType componentType = type.asArrayType().getComponentType();
			return new ArraySchema().items(registry.getSchemaOrReferenceFor(componentType));
		}
		return null;
	}

}