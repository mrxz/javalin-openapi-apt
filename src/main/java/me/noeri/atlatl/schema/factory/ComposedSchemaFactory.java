package me.noeri.atlatl.schema.factory;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import me.noeri.atlatl.annotations.KnownType;
import me.noeri.atlatl.schema.SchemaRegistry;
import me.noeri.atlatl.utils.TypeUtils;

public class ComposedSchemaFactory implements SchemaFactory {

	@Override
	public Schema<?> createSchema(ResolvedType type, SchemaRegistry registry) {
		if(type.isReferenceType()) {
			ResolvedReferenceTypeDeclaration typeDeclaration = type.asReferenceType().getTypeDeclaration();
			ClassOrInterfaceDeclaration declaration = TypeUtils.getDeclarationFromResolvedReferenceTypeDeclaration(typeDeclaration);
			if(declaration != null && declaration.isAnnotationPresent(KnownType.class)) {
				return declaration.getAnnotations().stream()
					.filter(annotation -> annotation.resolve().getId().equals(KnownType.class.getName()))
					.map(AnnotationExpr::asSingleMemberAnnotationExpr)
					.map(SingleMemberAnnotationExpr::getMemberValue)
					.map(Expression::asClassExpr)
					.map(ClassExpr::getType)
					.map(Type::resolve)
					.map(registry::getSchemaOrReferenceFor)
					.reduce(new ComposedSchema(), (acc, additionalSchema) -> acc.addOneOfItem(additionalSchema), (a, b) -> {
						a.getOneOf().addAll(b.getOneOf());
						return a;
					});
			}
		}
		return null;
	}

}
