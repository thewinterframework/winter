package com.thewinterframework.paper.listener;

import com.thewinterframework.component.annotation.Component;
import com.thewinterframework.component.annotation.HandledBy;
import com.thewinterframework.component.annotation.SyncLoading;
import com.thewinterframework.paper.listener.handler.ListenerComponentHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A scope annotation for listener components.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@HandledBy(ListenerComponentHandler.class)
@SyncLoading
@Deprecated(forRemoval = false)
public @interface ListenerComponent {
}
