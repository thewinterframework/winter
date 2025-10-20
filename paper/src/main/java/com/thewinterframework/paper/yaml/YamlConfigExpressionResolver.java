package com.thewinterframework.paper.yaml;

import com.google.inject.Key;
import com.thewinterframework.expression.ConfigParsedExpression;
import com.thewinterframework.expression.SimpleFirstExpressionResolver;
import com.thewinterframework.paper.PaperWinterPlugin;
import com.thewinterframework.utils.reflect.AnnotatedMethodHandle;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public class YamlConfigExpressionResolver extends SimpleFirstExpressionResolver {

	private final PaperWinterPlugin plugin;

	public YamlConfigExpressionResolver(PaperWinterPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	protected <T> @Nullable T resolveComplexExpression(AnnotatedMethodHandle<?> method, String expression, Class<T> returnType) {
		final var parsedExpression = ConfigParsedExpression.parse(expression);
		final var fileName = parsedExpression.configName();
		final var path = parsedExpression.path();

		final var yamlInstance = plugin.getInjector().getInstance(Key.get(YamlConfig.class, new FileName() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return FileName.class;
			}

			@Override
			public String value() {
				return fileName;
			}
		}));

		if (yamlInstance == null) {
			return null;
		}

		final var value = yamlInstance.get(path);
		return resolveSimple(String.valueOf(value), returnType);
	}
}
