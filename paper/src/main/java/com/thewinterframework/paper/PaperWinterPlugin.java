package com.thewinterframework.paper;

import com.google.inject.Module;
import com.google.inject.*;
import com.thewinterframework.plugin.DataFolder;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.PluginModuleManager;
import com.thewinterframework.utils.TimeUnit;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class PaperWinterPlugin extends JavaPlugin implements WinterPlugin {

	private final PluginModuleManager moduleManager = new PluginModuleManager(this);

	protected @Inject Injector injector;

	@Override
	public final void onLoad() {
		final var startTime = System.currentTimeMillis();
		if (!moduleManager.scanModules() || !moduleManager.loadModules()) {
			Bukkit.getPluginManager().disablePlugin(this);
		}

		onPluginLoad();
		getSLF4JLogger().info("Plugin loaded in {}ms", System.currentTimeMillis() - startTime);
	}

	@Override
	public final void onEnable() {
		final var startTime = System.currentTimeMillis();
		final var injector = createInjector();
		injector.injectMembers(this);

		if (!moduleManager.injectModules() || !moduleManager.enableModules()) {
			Bukkit.getPluginManager().disablePlugin(this);
		}

		onPluginEnable();
		getSLF4JLogger().info("Plugin enabled in {}ms", System.currentTimeMillis() - startTime);
	}

	protected Injector createInjector() {
		return Guice.createInjector(getGuiceStage(), getGuiceModules());
	}

	@Override
	public final void onDisable() {
		final var startTime = System.currentTimeMillis();
		if (!moduleManager.disableModules()) {
			Bukkit.getPluginManager().disablePlugin(this);
		}

		onPluginDisable();
		getSLF4JLogger().info("Plugin disabled in {}ms", System.currentTimeMillis() - startTime);
	}

	@Override
	public final int scheduleRepeatingTask(Runnable task, long delay, long period, TimeUnit unit, boolean async) {
		final var delayTicks = unit.toTicks(delay);
		final var periodTicks = unit.toTicks(period);
		if (async) {
			return Bukkit.getScheduler().runTaskTimerAsynchronously(this, task, delayTicks, periodTicks).getTaskId();
		} else {
			return Bukkit.getScheduler().runTaskTimer(this, task, delayTicks, periodTicks).getTaskId();
		}
	}

	@Override
	public void cancelTask(int taskId) {
		Bukkit.getScheduler().cancelTask(taskId);
	}

	@Override
	public Injector getInjector() {
		return injector;
	}

	@Override
	public @NotNull Logger getSLF4JLogger() {
		return super.getSLF4JLogger();
	}

	@Override
	public final PluginModuleManager getModuleManager() {
		return moduleManager;
	}

	protected List<Module> getGuiceModules() {
		final var modules = new ArrayList<Module>();
		modules.add(binder -> {
			binder.bind(WinterPlugin.class).toInstance(this);
			binder.bind(Plugin.class).toInstance(this);
			binder.bind(JavaPlugin.class).toInstance(this);
			binder.bind(Logger.class).toInstance(this.getSLF4JLogger());
			binder.bind(Path.class).annotatedWith(DataFolder.class).toInstance(this.getDataFolder().toPath());
		});

		modules.addAll(moduleManager.asGuiceModules());
		modules.add(this);

		return modules;
	}

	protected Stage getGuiceStage() {
		return Stage.PRODUCTION;
	}

	@Override
	@KeyPattern.Namespace
	@SuppressWarnings("all") // lol
	public @NotNull String namespace() {
		return this.getName().toLowerCase();
	}
}
