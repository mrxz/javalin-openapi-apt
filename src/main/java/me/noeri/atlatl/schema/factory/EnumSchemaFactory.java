package me.noeri.atlatl.schema.factory;

import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.List;
import java.util.stream.Collectors;
import me.noeri.atlatl.schema.SchemaRegistry;

public class EnumSchemaFactory implements SchemaFactory {
	@Override
	public Schema<?> createSchema(ResolvedType type, SchemaRegistry registry) {
		if(type.isReferenceType() && type.asReferenceType().getTypeDeclaration().isEnum()) {
			List<String> values = type.asReferenceType().getTypeDeclaration().asEnum().getEnumConstants()
					.stream()
					.map(ResolvedEnumConstantDeclaration::getName)
					.collect(Collectors.toList());
			return new StringSchema()._enum(values);
		}
		return null;
	}

}
