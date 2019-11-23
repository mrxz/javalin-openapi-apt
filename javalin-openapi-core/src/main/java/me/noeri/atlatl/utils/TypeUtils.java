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
package me.noeri.atlatl.utils;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

public final class TypeUtils {

	public static boolean isAssignable(ResolvedType reference, ResolvedReferenceTypeDeclaration type) {
		// Note: for some reasons JP/JSS' isAssignableBy methods don't seem to work
		if(!reference.isReferenceType()) {
			return false;
		}
		ResolvedReferenceType referenceType = reference.asReferenceType();
		if(referenceType.getId().equals(type.getId())) {
			return true;
		}
		return referenceType.getTypeDeclaration().getAllAncestors().stream()
				.anyMatch(ancestorReferenceType -> ancestorReferenceType.getId().equals(type.getId()));
	}

	public static ClassOrInterfaceDeclaration getDeclarationFromResolvedReferenceTypeDeclaration(ResolvedReferenceTypeDeclaration typeDeclaration) {
		if(typeDeclaration.isInterface()) {
			return typeDeclaration.asInterface().toAst().orElse(null);
		}
		if(typeDeclaration.isClass()) {
			return typeDeclaration.asClass().toAst()
					.flatMap(node -> node.findFirst(ClassOrInterfaceDeclaration.class))
					.orElse(null);
		}
		return null;
	}

}
