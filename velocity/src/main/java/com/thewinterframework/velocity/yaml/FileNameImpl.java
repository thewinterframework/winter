package com.thewinterframework.velocity.yaml;

import java.lang.annotation.Annotation;

public record FileNameImpl(String value) implements FileName {
	@Override
	public Class<? extends Annotation> annotationType() {
		return FileName.class;
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof FileName fileName && value.equals(fileName.value());
	}

	@Override
	public int hashCode() {
		return 127 * "value".hashCode() ^ value.hashCode();
	}
}
