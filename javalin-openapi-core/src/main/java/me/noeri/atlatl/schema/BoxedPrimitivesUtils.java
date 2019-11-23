/*
 * Copyright (c) 2019 Noeri Huisman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
