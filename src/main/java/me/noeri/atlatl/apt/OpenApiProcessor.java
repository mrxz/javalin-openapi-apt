package me.noeri.atlatl.apt;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.auto.service.AutoService;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import me.noeri.atlatl.AstAnalyze;
import me.noeri.atlatl.annotations.OpenApi;
import me.noeri.atlatl.apt.eclipse.EclipseCompilerContext;

@SupportedAnnotationTypes("me.noeri.atlatl.annotations.OpenApi")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class OpenApiProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Filer filer = processingEnv.getFiler();
		if(annotations.isEmpty()) {
			return false;
		}

		// Note: currently only Javac is supported
		//CompilerContext compilerContext = new JavaCompilerContext(processingEnv, roundEnv);
		CompilerContext compilerContext = new EclipseCompilerContext(processingEnv, roundEnv);

		Set<? extends Element> targetElements = roundEnv.getElementsAnnotatedWith(OpenApi.class);
		for(Element targetElement : targetElements) {
			OpenApi annotation = targetElement.getAnnotationsByType(OpenApi.class)[0];

			TypeElement classElement = (TypeElement) targetElement.getEnclosingElement();
			String routesClass = classElement.getQualifiedName().toString();
			String routesMethod = targetElement.getSimpleName().toString();

			// Derive source path
			String sourcePath = compilerContext.getSourcePath();

			TypeSolver typeSolver = new CombinedTypeSolver(
					new ReflectionTypeSolver(true),
					compilerContext.getTypeSolver(),
					new JavaParserTypeSolver(sourcePath));
			AstAnalyze analyzer = new AstAnalyze(typeSolver, new AptReporter(processingEnv));
			OpenAPI result = analyzer.analyze(sourcePath, routesClass, routesMethod);

			// Set information from the annotation
			result.setInfo(new Info()
					.title(annotation.title())
					.version(annotation.version())
					.description(annotation.description()));

			try {
				FileObject output = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", annotation.outputFile());
				try(OutputStream outputStream = output.openOutputStream()) {
					Yaml.pretty().writeValue(outputStream, result);
				}
			} catch(IOException e) {
				processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to write resulting openapi file: " + annotation.outputFile());
			}
		}
		return false;
	}
}
