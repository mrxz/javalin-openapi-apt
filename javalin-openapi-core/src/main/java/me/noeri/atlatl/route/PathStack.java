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
			pathSegment = pathSegment.substring(0, pathSegment.length() - 1);
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
