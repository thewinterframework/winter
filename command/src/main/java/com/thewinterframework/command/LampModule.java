package com.thewinterframework.command;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.utils.Reflections;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.parameter.ContextParameter;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.process.CommandCondition;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.response.ResponseHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class LampModule implements PluginModule {

	private final List<Class<?>> commandComponents = new ArrayList<>();

	@Override
	public void configure(Binder binder) {
		binder.bindScope(CommandComponent.class, Scopes.SINGLETON);
		binder.bind(LampProvider.class)
				.to(LampInstance.class)
				.in(Scopes.SINGLETON);
	}

	@Override
	public boolean onLoad(WinterPlugin plugin) {
		try {
			final var components = CommandComponentProcessor.scan(plugin.getClass(), CommandComponent.class).getClassList();
			commandComponents.addAll(components);
			return true;
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			plugin.getSLF4JLogger().error("Failed to scan command components", e);
			return false;
		}
	}

	@Override
	public boolean onEnable(WinterPlugin plugin) {
		final var injector = plugin.getInjector();
		final var lampInstance = (LampInstance) injector.getInstance(LampProvider.class);
		final var lampBuilder = BukkitLamp.builder((JavaPlugin) plugin);

		for (final var commandComponent : commandComponents) {
			if (ParameterType.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<? extends ParameterType<?, ?>>) commandComponent;
				registerParameterType(clazz, lampBuilder, injector);
			}

			if (ParameterType.Factory.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<? extends ParameterType.Factory<BukkitCommandActor>>) commandComponent;
				registerParameterTypeFactory(clazz, lampBuilder, injector);
			}

			if (CommandSuggestions.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<? extends CommandSuggestions<BukkitCommandActor, ?>>) commandComponent;
				registerSuggestionProvider(clazz, lampBuilder, injector);
			}

			if (SuggestionProvider.Factory.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<? extends SuggestionProvider.Factory<BukkitCommandActor>>) commandComponent;
				registerSuggestionProviderFactory(clazz, lampBuilder, injector);
			}

			if (ContextParameter.Factory.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<? extends ContextParameter.Factory<BukkitCommandActor>>) commandComponent;
				registerContextParameterFactory(clazz, lampBuilder, injector);
			}

			if (CommandPermission.Factory.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<CommandPermission.Factory<BukkitCommandActor>>) commandComponent;
				lampBuilder.permissionFactory(injector.getInstance(clazz));
			}

			if (ParameterValidator.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<ParameterValidator<BukkitCommandActor, ?>>) commandComponent;
				registerParameterValidator(clazz, lampBuilder, injector);
			}

			if (CommandCondition.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<? extends CommandCondition<BukkitCommandActor>>) commandComponent;
				lampBuilder.commandCondition(injector.getInstance(clazz));
			}

			if (ResponseHandler.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<? extends ResponseHandler<BukkitCommandActor, ?>>) commandComponent;
				registerResponseHandler(clazz, lampBuilder, injector);
			}

			if (ResponseHandler.Factory.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<? extends ResponseHandler.Factory<BukkitCommandActor>>) commandComponent;
				lampBuilder.responseHandler(injector.getInstance(clazz));
			}

			if (CommandExceptionHandler.class.isAssignableFrom(commandComponent)) {
				final var clazz = (Class<? extends CommandExceptionHandler<BukkitCommandActor>>) commandComponent;
				lampBuilder.exceptionHandler(injector.getInstance(clazz));
			}
		}

		final var lamp = lampBuilder.build();
		for (final var commandComponent : commandComponents) {
			lamp.register(injector.getInstance(commandComponent));
		}

		lampInstance.setLamp(lamp);
		return true;
	}

	@Override
	public boolean onDisable(WinterPlugin plugin) {
		return true;
	}

	private void registerResponseHandler(
			Class<? extends ResponseHandler<BukkitCommandActor, ?>> clazz,
			Lamp.Builder<BukkitCommandActor> builder,
			Injector injector
	) {
		final var parameterGenericType = Reflections.getGenericType(clazz, ResponseHandler.class, 1);
		final var parameterGenericClazz = (Class<Object>) Reflections.toClass(parameterGenericType);
		final var instance = (ResponseHandler<BukkitCommandActor, Object>) injector.getInstance(clazz);
		builder.responseHandler(parameterGenericClazz, instance);
	}

	private void registerParameterValidator(
			Class<? extends ParameterValidator<BukkitCommandActor, ?>> clazz,
			Lamp.Builder<BukkitCommandActor> builder,
			Injector injector
	) {
		final var parameterGenericType = Reflections.getGenericType(clazz, ParameterValidator.class, 1);
		final var parameterGenericClazz = (Class<Object>) Reflections.toClass(parameterGenericType);
		final var instance = (ParameterValidator<BukkitCommandActor, Object>) injector.getInstance(clazz);
		builder.parameterValidator(parameterGenericClazz, instance);
	}

	private void registerContextParameterFactory(
			Class<? extends ContextParameter.Factory<BukkitCommandActor>> suggestionProviderFactory,
			Lamp.Builder<BukkitCommandActor> builder,
			Injector injector
	) {
		builder.parameterTypes().addContextParameterFactory(injector.getInstance(suggestionProviderFactory));
	}

	private void registerSuggestionProvider(
			Class<? extends CommandSuggestions<BukkitCommandActor, ?>> commandSuggestions,
			Lamp.Builder<BukkitCommandActor> builder,
			Injector injector
	) {
		final var parameterGenericType = Reflections.getGenericType(commandSuggestions, CommandSuggestions.class, 1);
		final var parameterGenericClazz = (Class<Object>) Reflections.toClass(parameterGenericType);
		final var instance = (CommandSuggestions<BukkitCommandActor, Object>) injector.getInstance(commandSuggestions);
		builder.suggestionProviders().addProvider(parameterGenericClazz, instance);
	}

	private void registerSuggestionProviderFactory(
			Class<? extends SuggestionProvider.Factory<BukkitCommandActor>> suggestionProviderFactory,
			Lamp.Builder<BukkitCommandActor> builder,
			Injector injector
	) {
		builder.suggestionProviders().addProviderFactory(injector.getInstance(suggestionProviderFactory));
	}

	private void registerParameterType(
			Class<? extends ParameterType<?, ?>> parameterTypeClazz,
			Lamp.Builder<BukkitCommandActor> builder,
			Injector injector
	) {
		final var parameterGenericType = Reflections.getGenericType(parameterTypeClazz, ParameterType.class, 1);
		final var parameterGenericClazz = (Class<Object>) Reflections.toClass(parameterGenericType);
		final var parameterTypeInstance = (ParameterType<BukkitCommandActor, Object>) injector.getInstance(parameterTypeClazz);
		builder.parameterTypes().addParameterType(parameterGenericClazz, parameterTypeInstance);
	}

	private void registerParameterTypeFactory(
			Class<? extends ParameterType.Factory<BukkitCommandActor>> parameterTypeClazz,
			Lamp.Builder<BukkitCommandActor> builder,
			Injector injector
	) {
		builder.parameterTypes().addParameterTypeFactory(injector.getInstance(parameterTypeClazz));
	}
}
