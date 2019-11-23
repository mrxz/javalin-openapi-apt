/*
 * Copyright 2019 Noeri Huisman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.javaparser.symbolsolver.javassistmodel;

import com.github.javaparser.resolution.declarations.ResolvedTypeParametrizable;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import javassist.bytecode.SignatureAttribute;

public class JavassistUtilsTrampoline {

	public static ResolvedType signatureTypeToType(SignatureAttribute.Type signatureType, TypeSolver typeSolver, ResolvedTypeParametrizable typeParametrizable) {
		return JavassistUtils.signatureTypeToType(signatureType, typeSolver, typeParametrizable);
	}

}
