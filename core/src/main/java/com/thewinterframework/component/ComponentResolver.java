package com.thewinterframework.component;

import net.kyori.adventure.text.Component;

public interface ComponentResolver<T> {

	ComponentResolver<Object> DEFAULT = t -> Component.text(t.toString());

	Component resolve(T t);
}
