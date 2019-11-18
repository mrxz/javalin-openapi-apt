package me.noeri.atlatl.apt.eclipse;

import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import me.noeri.atlatl.utils.FileUtils;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.Compiler;

public class IdeEclipseCompilerContext extends EclipseCompilerContext<IdeProcessingEnvImpl> {

	public IdeEclipseCompilerContext(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment) {
		super(processingEnvironment, roundEnvironment, IdeProcessingEnvImpl.class);
	}

	@Override
	public String getSourcePath() {
		Compiler compiler = processingEnvironment.getCompiler();
		CompilationUnitDeclaration cud = Stream.of(compiler.unitsToProcess)
				.filter(unit -> unit.getFileName().length > 0)
				.findFirst().orElse(null);

		String sourceClass = cud.currentPackage.toString() + "." + new String(cud.getMainTypeName());
		String relativeSourceFilePath = FileUtils.qualifiedNameToFile(sourceClass);

		try {
			IJavaProject javaProject = processingEnvironment.getJavaProject();
			String projectPath = javaProject.getProject().getLocation().toOSString();

			String sourceClassPath = javaProject.findType(sourceClass).getPath()
					.makeRelativeTo(javaProject.getPath()).toOSString();
			String sourceFilePath = projectPath + "/" + sourceClassPath;

			return sourceFilePath.substring(0, sourceFilePath.length() - relativeSourceFilePath.length());
		} catch(JavaModelException e) {
			throw new RuntimeException("Failed to derive source path", e);
		}

	}
}
