package me.noeri.atlatl.apt.eclipse;

import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import me.noeri.atlatl.utils.FileUtils;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

public class BatchEclipseCompilerContext extends EclipseCompilerContext<BaseProcessingEnvImpl> {

	public BatchEclipseCompilerContext(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment) {
		super(processingEnvironment, roundEnvironment, BaseProcessingEnvImpl.class);
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
}
