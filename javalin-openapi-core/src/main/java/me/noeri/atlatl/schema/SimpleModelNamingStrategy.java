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
import java.util.stream.Collectors;

public class SimpleModelNamingStrategy implements ModelNamingStrategy {

	@Override
	public String convert(ResolvedType resolvedType) {
		String[] parts = getName(resolvedType).split("\\.");
		String simpleName = parts[parts.length - 1];
		// Note: strip DTO
		if(simpleName.endsWith("Dto")) {
			simpleName = simpleName.replaceAll("Dto$", "");
		}

		if(resolvedType.isReferenceType() && !resolvedType.asReferenceType().getTypeParametersMap().isEmpty()) {
			simpleName += "_";
			simpleName += resolvedType.asReferenceType().typeParametersMap().getTypes().stream()
				.map(this::convert)
				.collect(Collectors.joining());
			simpleName += "_";
		}
		return simpleName;
	}

	private String getName(ResolvedType resolvedType) {
		if(resolvedType.isReferenceType()) {
			return resolvedType.asReferenceType().getId();
		}
		// FIXME
		return resolvedType.isWildcard() ? "Wildcard" : "???";
	}
}
