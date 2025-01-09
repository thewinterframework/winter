package com.thewinterframework.utils;

import java.util.ArrayList;
import java.util.List;

public class Collections {
	public static <T> List<T> repeat(T element, int count) {
		final var list = new ArrayList<T>();
		for (var i = 0; i < count; i++) {
			list.add(element);
		}
		return list;
	}
}
