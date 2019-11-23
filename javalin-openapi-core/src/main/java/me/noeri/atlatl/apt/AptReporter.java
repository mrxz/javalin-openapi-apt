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
package me.noeri.atlatl.apt;

import java.util.Optional;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import me.noeri.atlatl.Reporter;

public class AptReporter implements Reporter {

	private final Elements elementUtils;
	private final Messager messager;

	public AptReporter(ProcessingEnvironment procesingEnvironment) {
		this.elementUtils = procesingEnvironment.getElementUtils();
		this.messager = procesingEnvironment.getMessager();
	}

	@Override
	public void report(MessageKind kind, String message, String qualifiedName) {
		String className = qualifiedName;
		final String methodName;
		if(qualifiedName.contains("#")) {
			String[] parts = qualifiedName.split("#", 2);
			className = parts[0];
			methodName = parts[1];
		} else {
			methodName = null;
		}

		Element typeElement = elementUtils.getTypeElement(className);
		if(methodName != null) {
			Optional<? extends Element> childElement = typeElement.getEnclosedElements().stream()
				.filter(enclosedElement -> enclosedElement.getSimpleName().toString().equals(methodName))
				.findAny();
			typeElement = childElement.map(ce -> (Element)ce).orElse(typeElement);
		}
		messager.printMessage(convertMessageKind(kind), message, typeElement);
	}

	private Kind convertMessageKind(MessageKind kind) {
		if(kind == MessageKind.ERROR) {
			return Kind.ERROR;
		}
		return Kind.MANDATORY_WARNING;
	}

}
