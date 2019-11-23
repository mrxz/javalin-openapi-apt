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
package me.noeri.atlatl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.UnsolvedSymbolException;
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
import me.noeri.atlatl.schema.SchemaRegistry;
import me.noeri.atlatl.schema.factory.SchemaRegistryFactory;
import me.noeri.atlatl.utils.FileUtils;

public class AstAnalyze {
	private final JavaParser parser;
	private final Reporter reporter;
	private final RouteAnalyzer routeAnalyzer;
	private final SchemaRegistry schemaRegistry;
	private final OperationParser operationParser;

	public AstAnalyze(TypeSolver typeSolver, Reporter reporter) {
		JavaSymbolSolver symbolResolver = new JavaSymbolSolver(typeSolver);
		parser = new JavaParser(new ParserConfiguration().setSymbolResolver(symbolResolver));
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
			CompilationUnit compilationUnit = parse(apiRoutesFile);
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
					try {
						CompilationUnit controllerCompilationUnit = compilationUnitCache.computeIfAbsent(controllerId,
								id -> parse(Paths.get(base, FileUtils.qualifiedNameToFile(id)).toFile()));
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
					} catch(ParseProblemException | UnsolvedSymbolException e) {
						String message = String.format("Failed to parse method %s of %s: %s", methodName, controllerId, e.getMessage());
						reporter.error(message, controllerId + "#" + methodName);
					}
				});
			} else {
				reporter.error("Failed to find annotated field: " + apiRoutesField, apiRoutesClass);
			}

			// Schemas
			for(Entry<String, Schema<?>> schema : schemaRegistry.getSchemas().entrySet()) {
				components.addSchemas(schema.getKey(), schema.getValue());
			}
		} catch(ParseProblemException e) {
			reporter.error("Failed to parse " + apiRoutesClass + ": " + e.getMessage(), apiRoutesClass);
		}
		return openApi;
	}

	private CompilationUnit parse(File file) {
		try {
			ParseResult<CompilationUnit> result = parser.parse(file);
			if(result.isSuccessful()) {
				return result.getResult().get();
			}
			throw new ParseProblemException(result.getProblems());
		} catch(FileNotFoundException e) {
			throw new ParseProblemException(e);
		}
	}
}
