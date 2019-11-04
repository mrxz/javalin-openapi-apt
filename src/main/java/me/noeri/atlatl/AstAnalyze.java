package me.noeri.atlatl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import me.noeri.atlatl.operation.OperationParser;
import me.noeri.atlatl.route.RouteAnalyzer;
import me.noeri.atlatl.schema.ArraySchemaFactory;
import me.noeri.atlatl.schema.BoxedPrimitivesSchemaFactory;
import me.noeri.atlatl.schema.CollectionSchemaFactory;
import me.noeri.atlatl.schema.EnumSchemaFactory;
import me.noeri.atlatl.schema.MapSchemaFactory;
import me.noeri.atlatl.schema.PrimitiveSchemaFactory;
import me.noeri.atlatl.schema.ReferenceSchemaFactory;
import me.noeri.atlatl.schema.SchemaRegistry;
import me.noeri.atlatl.schema.SchemaRegistryFactory;
import me.noeri.atlatl.schema.SimpleModelNamingStrategy;
import me.noeri.atlatl.utils.FileUtils;

public class AstAnalyze {
	private final Reporter reporter;
	private final RouteAnalyzer routeAnalyzer;
	private final SchemaRegistry schemaRegistry;
	private final OperationParser operationParser;

	public AstAnalyze(TypeSolver typeSolver, Reporter reporter) {
		JavaSymbolSolver symbolResolver = new JavaSymbolSolver(typeSolver);
		StaticJavaParser
			.getConfiguration()
			.setSymbolResolver(symbolResolver);
		this.reporter = reporter;
		routeAnalyzer = new RouteAnalyzer(typeSolver);
		schemaRegistry = SchemaRegistryFactory.createDefaultRegistry(typeSolver);
		operationParser = new OperationParser(typeSolver, schemaRegistry, reporter);
	}

	public OpenAPI analyze(String base, String apiRoutesClass, String apiRoutesField) {
		OpenAPI openApi = new OpenAPI();
		Components components = new Components();
		openApi.setComponents(components);
		io.swagger.v3.oas.models.Paths paths = new io.swagger.v3.oas.models.Paths();
		openApi.setPaths(paths);

		try {
			File apiRoutesFile =  Paths.get(base, FileUtils.qualifiedNameToFile(apiRoutesClass)).toFile();
			CompilationUnit compilationUnit = StaticJavaParser.parse(apiRoutesFile);
			Optional<FieldDeclaration> declaration  = compilationUnit
					.findFirst(FieldDeclaration.class, field -> field.getVariable(0).getName().toString().equals(apiRoutesField));
			if(declaration.isPresent()) {
				Map<String, CompilationUnit> compilationUnitCache = new HashMap<>();
				FieldDeclaration field = declaration.get();
				routeAnalyzer.analyze(field).forEach(route -> {
					if(!route.getAction().isMethodReferenceExpr()) {
						reporter.error("Action expression must be a method reference (" + route.getMethod() + " " + route.getPath() + ")", field);
						return;
					}
					ResolvedType controllerType = route.getAction().asMethodReferenceExpr().getScope().calculateResolvedType();
					String controllerId = controllerType.asReferenceType().getQualifiedName();
					String controllerName = controllerType.asReferenceType().getTypeDeclaration().getName();
					String methodName = route.getAction().asMethodReferenceExpr().getIdentifier();

					CompilationUnit controllerCompilationUnit = compilationUnitCache.computeIfAbsent(controllerId, id -> {
						try {
							return StaticJavaParser.parse(Paths.get(base, FileUtils.qualifiedNameToFile(id)).toFile());
						} catch(FileNotFoundException e) {
							reporter.error("Failed to resolve method (" + route.getMethod() + " " + route.getPath() + ")", field);
						}
						return null;
					});
					MethodDeclaration method = controllerCompilationUnit
							.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals(methodName))
							.get();
					Operation operation = operationParser.analyzeOperation(method, methodName);
					// FIXME: Make tag resolution configurable
					operation.setTags(Arrays.asList(controllerName.replaceAll("Controller$", "")));

					PathItem item = paths.computeIfAbsent(route.getPath(), x -> new PathItem());
					switch(route.getMethod()) {
					case "GET": item.get(operation); break;
					case "POST": item.post(operation); break;
					case "PUT": item.put(operation); break;
					case "DELETE": item.delete(operation); break;
					}
				});
			} else {
				reporter.error("Failed to find annotated field: " + apiRoutesField, apiRoutesClass);
			}

			// Schemas
			for(Entry<String, Schema<?>> schema : schemaRegistry.getSchemas().entrySet()) {
				components.addSchemas(schema.getKey(), schema.getValue());
			}
		} catch(FileNotFoundException e) {
			reporter.error("File containing api routes class " + apiRoutesClass + " not found", apiRoutesClass);
		}
		return openApi;
	}
}
