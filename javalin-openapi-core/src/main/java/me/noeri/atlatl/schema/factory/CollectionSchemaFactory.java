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
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import me.noeri.atlatl.schema.SchemaRegistry;
import me.noeri.atlatl.utils.TypeUtils;

public class CollectionSchemaFactory implements SchemaFactory {
	private final ResolvedReferenceTypeDeclaration collectionType;

	public CollectionSchemaFactory(TypeSolver typeSolver) {
		this.collectionType = typeSolver.solveType("java.util.Collection").asReferenceType();
	}

	@Override
	public Schema<?> createSchema(ResolvedType type, SchemaRegistry registry) {
		if(TypeUtils.isAssignable(type, collectionType)) {
			ResolvedType elementType = type.asReferenceType().typeParametersValues().get(0);
			Schema<?> elementSchema = registry.getSchemaOrReferenceFor(elementType);
			return new ArraySchema().items(elementSchema);
		}
		return null;
	}

}
