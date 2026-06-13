package com.thewinterframework.processor.template;

import com.thewinterframework.processor.context.ProcessorContext;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TemplateBuilder {

	private final String template;
	private final Map<String, String> placeholders = new HashMap<>();
	private final Map<String, List<Map<String, String>>> loops = new HashMap<>();
	private final List<UnaryOperator<String>> processors = new ArrayList<>();

	private TemplateBuilder(final String template) {
		this.template = template;
	}

	public static TemplateBuilder fromResource(final String path) {
		try (final var is = TemplateBuilder.class.getClassLoader().getResourceAsStream(path)) {
			if (is == null) {
				throw new IllegalArgumentException("Template not found: " + path);
			}
			final var content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			return new TemplateBuilder(content);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public TemplateBuilder placeholder(final String key, final String value) {
		placeholders.put(key, value);
		return this;
	}

	/**
	 * Permite inyectar lógica de procesamiento personalizada (Regex, transformaciones, etc.)
	 * sobre el texto después de haber procesado loops y placeholders.
	 */
	public TemplateBuilder custom(final UnaryOperator<String> processor) {
		this.processors.add(processor);
		return this;
	}

	public EachBuilder each(final String id) {
		return new EachBuilder(this, id);
	}

	public <T> TemplateBuilder withEach(final String id, final Iterable<T> iterable, final BiConsumer<EachBuilder, T> action) {
		final var eachBuilder = new EachBuilder(this, id);
		for (final var t : iterable) {
			action.accept(eachBuilder, t);
		}
		return eachBuilder.end();
	}

	public static final class EachBuilder {
		private final TemplateBuilder engine;
		private final String id;
		private final List<Map<String, String>> items = new ArrayList<>();

		private EachBuilder(final TemplateBuilder engine, final String id) {
			this.engine = engine;
			this.id = id;
		}

		public EachBuilder add(final Map<String, String> values) {
			items.add(values);
			return this;
		}

		public EachBuilder add(final String key, final String value) {
			final var map = new HashMap<String, String>();
			map.put(key, value);
			items.add(map);
			return this;
		}

		public TemplateBuilder end() {
			engine.loops.put(id, items);
			return engine;
		}
	}

	public String render() {
		var result = template;
		for (final var entry : loops.entrySet()) {
			result = processLoop(result, entry.getKey(), entry.getValue());
		}

		for (final var entry : placeholders.entrySet()) {
			result = result.replace("<" + entry.getKey() + ">", entry.getValue());
		}

		for (final var processor : processors) {
			result = processor.apply(result);
		}

		return result;
	}

	private String processLoop(final String input, final String id, final List<Map<String, String>> items) {
		final var pattern = Pattern.compile(
				"<EACH_" + id + ">([\\s\\S]*?)</EACH_" + id + ">",
				Pattern.MULTILINE
		);

		final var matcher = pattern.matcher(input);
		final var sb = new StringBuilder();

		while (matcher.find()) {
			final var block = matcher.group(1);
			final var repeated = new StringBuilder();

			for (final var item : items) {
				var processed = block;
				for (final var entry : item.entrySet()) {
					processed = processed.replace("<" + entry.getKey() + ">", entry.getValue());
				}
				repeated.append(processed);
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(repeated.toString()));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public void write(final ProcessorContext ctx, final CharSequence name) {
		write(ctx.filer(), name);
	}

	public void write(final Filer filer, final CharSequence fullyQualifiedName) {
		try {
			final var file = filer.createSourceFile(fullyQualifiedName);
			try (final var writer = file.openWriter()) {
				writer.write(render());
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}