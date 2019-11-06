package me.noeri.atlatl.schema.factory;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import me.noeri.atlatl.schema.SchemaRegistry;
import me.noeri.atlatl.schema.SimpleModelNamingStrategy;

public final class SchemaRegistryFactory {

	public static SchemaRegistry createDefaultRegistry(TypeSolver typeSolver) {
		return new SchemaRegistry(new SimpleModelNamingStrategy(),
				new PrimitiveSchemaFactory(),
				new BoxedPrimitivesSchemaFactory(),
				new ArraySchemaFactory(),
				new EnumSchemaFactory(),
				new CollectionSchemaFactory(typeSolver),
				new MapSchemaFactory(typeSolver),
				new ComposedSchemaFactory(),
				new ReferenceSchemaFactory());
	}

}
