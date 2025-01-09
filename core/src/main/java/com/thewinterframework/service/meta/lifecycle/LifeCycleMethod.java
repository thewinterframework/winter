package com.thewinterframework.service.meta.lifecycle;

import com.thewinterframework.utils.Reflections.AnnotatedMethodHandle;

public record LifeCycleMethod(Class<?> service, AnnotatedMethodHandle<?> method, Class<?>[] before, Class<?>[] after) {}
