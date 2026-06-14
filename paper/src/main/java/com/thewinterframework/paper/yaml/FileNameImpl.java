package com.thewinterframework.paper.yaml;

import java.lang.annotation.Annotation;

public record FileNameImpl(String value) implements FileName {


	@Override
	public Class<? extends Annotation> annotationType() {
		return FileName.class;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof FileName)) return false;
		return value.equals(((FileName) o).value());
	}

	@Override
	public int hashCode() {
		return 127 * "value".hashCode() ^ value.hashCode();
	}
}