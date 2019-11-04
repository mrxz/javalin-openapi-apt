package me.noeri.atlatl.schema;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

public final class SchemaRegistryFactory {

	public static SchemaRegistry createDefaultRegistry(TypeSolver typeSolver) {
		return new SchemaRegistry(new SimpleModelNamingStrategy(),
				new PrimitiveSchemaFactory(),
				new BoxedPrimitivesSchemaFactory(typeSolver),
				new ArraySchemaFactory(),
				new EnumSchemaFactory(),
				new CollectionSchemaFactory(typeSolver),
				new MapSchemaFactory(typeSolver),
				new ReferenceSchemaFactory());
	}

}
