package me.noeri.atlatl.route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PathStack {
	private static final String PARAMETER_SIGNIFIER = ":";

	private final List<PathPart> parts;

	public PathStack() {
		this(new ArrayList<>());
	}

	public PathStack(Collection<PathPart> parts) {
		this.parts = new ArrayList<>(parts);
	}

	public void add(String pathSegment) {
		if(pathSegment.startsWith("/")) {
			pathSegment = pathSegment.substring(1);
		}
		if(pathSegment.endsWith("/")) {
			pathSegment = pathSegment.substring(0, pathSegment.length() - 2);
		}

		for(String part : pathSegment.split("/")) {
			addPart(part);
		}
	}

	public void addPart(String part) {
		if(part.startsWith(PARAMETER_SIGNIFIER)) {
			parts.add(new PathPart(part.substring(1), true));
			return;
		}
		parts.add(new PathPart(part, false));
	}

	public String fullPath() {
		return parts.stream()
				.map(part -> part.toString())
				.collect(Collectors.joining("/", "/", ""));
	}

	@Override
	public PathStack clone() {
		return new PathStack(parts);
	}

	private static class PathPart {
		private final String name;
		private final boolean parameter;

		PathPart(String name, boolean parameter) {
			this.name = name;
			this.parameter = parameter;
		}

		@Override
		public String toString() {
			return parameter ? String.format("{%s}", name) : name;
		}
	}

}
