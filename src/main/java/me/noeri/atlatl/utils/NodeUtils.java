package me.noeri.atlatl.utils;

import com.github.javaparser.ast.Node;
import java.util.Optional;

public final class NodeUtils {

	public static <N extends Node> Optional<N> findFirstAncestor(Class<N> type, Node node) {
		Optional<Node> current = node.getParentNode();
		while(current.isPresent() && !type.isAssignableFrom(current.get().getClass())) {
			current = current.flatMap(Node::getParentNode);
		}

		return current.map(n -> type.cast(n));
	}

}
