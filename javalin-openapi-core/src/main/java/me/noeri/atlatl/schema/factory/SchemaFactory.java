package me.noeri.atlatl.schema.factory;

import com.github.javaparser.resolution.types.ResolvedType;
import io.swagger.v3.oas.models.media.Schema;
import me.noeri.atlatl.schema.SchemaRegistry;

@FunctionalInterface
public interface SchemaFactory {

	public Schema<?> createSchema(ResolvedType type, SchemaRegistry registry);

}
