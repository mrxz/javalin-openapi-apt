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
package me.noeri.atlatl.schema.factory;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import me.noeri.atlatl.schema.SchemaRegistry;
import me.noeri.atlatl.utils.TypeUtils;

public class MapSchemaFactory implements SchemaFactory {
	private final ResolvedReferenceTypeDeclaration mapType;

	public MapSchemaFactory(TypeSolver typeSolver) {
		this.mapType = typeSolver.solveType("java.util.Map").asReferenceType();
	}

	@Override
	public Schema<?> createSchema(ResolvedType type, SchemaRegistry registry) {
		if(TypeUtils.isAssignable(type, mapType)) {
			ResolvedType elementType = type.asReferenceType().getTypeParametersMap().get(1).b;
			Schema<?> elementSchema = registry.getSchemaOrReferenceFor(elementType);
			return new ObjectSchema().additionalProperties(elementSchema);
		}
		return null;
	}

}
