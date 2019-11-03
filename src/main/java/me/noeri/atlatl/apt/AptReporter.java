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
