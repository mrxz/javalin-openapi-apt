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
package me.noeri.atlatl.apt.eclipse;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import me.noeri.atlatl.apt.CompilerContext;
import me.noeri.atlatl.apt.eclipse.model.EcjTypeSolver;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;

public abstract class EclipseCompilerContext<P extends BaseProcessingEnvImpl> implements CompilerContext {

	protected final P processingEnvironment;

	@SuppressWarnings("unchecked")
	public EclipseCompilerContext(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment, Class<P> envClass) {
		if(!envClass.isAssignableFrom(processingEnvironment.getClass())) {
			throw new IllegalArgumentException("Processing environment is invalid: " + processingEnvironment.getClass());
		}

		this.processingEnvironment = (P) processingEnvironment;
	}

	@Override
	public TypeSolver getTypeSolver() {
		return new EcjTypeSolver(processingEnvironment);
	}
}
