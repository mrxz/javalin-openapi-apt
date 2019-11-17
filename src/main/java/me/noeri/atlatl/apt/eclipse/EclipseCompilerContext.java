package me.noeri.atlatl.apt.eclipse;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import me.noeri.atlatl.apt.CompilerContext;
import me.noeri.atlatl.utils.FileUtils;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.Compiler;

public class EclipseCompilerContext implements CompilerContext {

	private final BaseProcessingEnvImpl processingEnvironment;
	private final RoundEnvironment roundEnvironment;

	public EclipseCompilerContext(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment) {
		System.out.println(processingEnvironment.getClass());
		if(!(processingEnvironment instanceof BaseProcessingEnvImpl)) {
			throw new IllegalArgumentException("Processing environment is invalid");
		}
		this.processingEnvironment = (BaseProcessingEnvImpl) processingEnvironment;
		this.roundEnvironment = roundEnvironment;
	}

	@Override
	public Collection<File> getDependencyJars() {
		Compiler compiler = processingEnvironment.getCompiler();
		for(CompilationUnitDeclaration unit : compiler.unitsToProcess) {
			System.out.println(new String(unit.getFileName()));
		}
		System.out.println("Ref bindings!");
		for(ReferenceBinding refBinding : compiler.referenceBindings) {
			System.out.println(new String(refBinding.sourceName));
		}
		return new ArrayList<>();
	}

	@Override
	public String getSourcePath() {
		Compiler compiler = processingEnvironment.getCompiler();
		CompilationUnitDeclaration cud = Stream.of(compiler.unitsToProcess)
				.filter(unit -> unit.getFileName().length > 0)
				.findFirst().orElse(null);

		String sourceFilePath = new String(cud.getFileName());
		String sourceClass = cud.currentPackage.toString() + "." + new String(cud.getMainTypeName());
		String relativeSourceFilePath = FileUtils.qualifiedNameToFile(sourceClass);
		return sourceFilePath.substring(0, sourceFilePath.length() - relativeSourceFilePath.length());
	}

	@Override
	public TypeSolver getTypeSolver() {
		return new EcjTypeSolver(processingEnvironment);
	}
}
