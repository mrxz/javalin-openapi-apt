package me.noeri.atlatl.schema;

import com.github.javaparser.resolution.types.ResolvedType;
import io.swagger.v3.oas.models.media.Schema;

public interface SchemaFactory {

	public Schema createSchema(ResolvedType type, SchemaRegistry registry);

}
