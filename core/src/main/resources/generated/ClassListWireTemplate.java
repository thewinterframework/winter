// This file is generated automatically by thewinterframework. Do not modify this file manually.
package <PACKAGE_NAME>;

import com.thewinterframework.processor.wire.ClassListWire;

import java.util.List;

public class <CLASS_NAME> implements ClassListWire {
	@Override
	public List<Class<?>> getWiredClasses() {
		return List.of(<WIRED_CLASSES>);
	}
}