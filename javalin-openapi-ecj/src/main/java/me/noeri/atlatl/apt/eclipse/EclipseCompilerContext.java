package me.noeri.atlatl.apt.eclipse;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import me.noeri.atlatl.apt.CompilerContext;
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
