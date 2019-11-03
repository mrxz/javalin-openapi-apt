package me.noeri.atlatl.schema;

import com.github.javaparser.resolution.types.ResolvedType;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class PrimitiveSchemaFactory implements SchemaFactory {

	@Override
	public Schema createSchema(ResolvedType type, SchemaRegistry registry) {
		if(type.isPrimitive()) {
			switch(type.asPrimitive()) {
			case BOOLEAN:
				return new BooleanSchema();
			case LONG:
			case INT:
			case SHORT:
			case FLOAT:
			case DOUBLE:
			case BYTE:
				return new NumberSchema();
			case CHAR:
				return new StringSchema().minLength(1).maxLength(1);
			}
		}
		return null;
	}

}
