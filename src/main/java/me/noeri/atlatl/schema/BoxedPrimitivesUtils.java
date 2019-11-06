package me.noeri.atlatl.schema;

import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Map;
import java.util.function.Supplier;

public final class BoxedPrimitivesUtils {
	private static final Map<String, Supplier<Schema<?>>> LOOKUP = ImmutableMap.<String, Supplier<Schema<?>>>builder()
			.put("java.lang.Boolean", BooleanSchema::new)
			.put("java.lang.Byte", NumberSchema::new)
			.put("java.lang.Character", () -> new StringSchema().maxLength(1))
			.put("java.lang.Float", NumberSchema::new)
			.put("java.lang.Integer", NumberSchema::new)
			.put("java.lang.Long", NumberSchema::new)
			.put("java.lang.Short", NumberSchema::new)
			.put("java.lang.Double", NumberSchema::new)
			.put("java.lang.String", StringSchema::new)
			.build();

	public static Schema<?> getSchemaFor(ResolvedType type) {
		return LOOKUP.getOrDefault(type.asReferenceType().getId(), () -> null).get();
	}
}
