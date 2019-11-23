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
import com.google.common.base.Predicates;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.noeri.atlatl.schema.factory.SchemaFactory;

public class SchemaRegistry {

	private final ModelNamingStrategy namingStrategy;
	private final Map<String, Schema<?>> schemas = new HashMap<>();
	private final List<SchemaFactory> factories;

	public SchemaRegistry(ModelNamingStrategy namingStrategy, SchemaFactory... factories) {
		this.namingStrategy = namingStrategy;
		this.factories = Arrays.asList(factories);
	}

	public Schema<?> getSchemaFor(ResolvedType type) {
		// FIXME: We need a reliable way to go from ResolvedType to a unique identifier...
		String name = namingStrategy.convert(type);
		Schema<?> existingSchema = name != null ? schemas.get(name) : null;
		if(existingSchema != null) {
			return existingSchema;
		}

		Schema<?> schema = factories.stream()
				.map(factory -> factory.createSchema(type, this))
				.filter(Predicates.notNull())
				.findFirst()
				.get();
		if(schema instanceof ObjectSchema) {
			schemas.put(name, schema);
		}
		return schema;
	}

	public Schema<?> getSchemaOrReferenceFor(ResolvedType type) {
		Schema<?> schema = getSchemaFor(type);
		// FIXME: Find a nicer way to handle the java.lang.Object case.
		if(type.isReferenceType() && type.asReferenceType().getId().equals("java.lang.Object")) {
			return new ObjectSchema();
		}
		if(schema instanceof ObjectSchema) {
			return new ObjectSchema().$ref("#/components/schemas/" + namingStrategy.convert(type));
		}
		return schema;
	}

	public Map<String, Schema<?>> getSchemas() {
		return schemas;
	}
}
