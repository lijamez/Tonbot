package com.tonberry.tonbot;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

import sx.blah.discord.api.IDiscordClient;

class PluginLoader {

	private static final Logger LOG = LoggerFactory.getLogger(PluginLoader.class);

	private final String configDir;

	@Inject
	public PluginLoader(@ConfigDir String configDir) {
		this.configDir = Preconditions.checkNotNull(configDir, "configDir must be non-null.");
	}

	/**
	 * Instantiates plugins based on the fully qualified names of their
	 * {@link TonbotPlugin}. Names of factories that do not exist in the classpath
	 * will be ignored. Any factories that return a null {@link PluginResources}
	 * will be ignored.
	 *
	 * @param pluginFqns
	 *            List of fully qualified {@link TonbotPlugin} class names.
	 * @param prefix
	 *            The prefix. Non-null.
	 * @param discordClient
	 *            The discord client. Non-null.
	 * @return A list of {@link PluginResources}s.
	 */
	@SuppressWarnings("unchecked")
	public List<TonbotPlugin> instantiatePlugins(List<String> pluginFqns, String prefix, IDiscordClient discordClient) {
		Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
		Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");

		return pluginFqns.stream()
				.map(String::trim)
				.map(pluginClassName -> {
					Class<?> pluginClass;
					try {
						pluginClass = Class.forName(pluginClassName);
					} catch (ClassNotFoundException e) {
						LOG.warn("Plugin '{}' could not be loaded because it was not found on the classpath.",
								pluginClassName);
						return null;
					}

					if (!TonbotPlugin.class.isAssignableFrom(pluginClass)) {
						LOG.warn("PluginResources '{}' could not be loaded because it is not a TonbotPlugin.",
								pluginClass);
						return null;
					}

					Constructor<TonbotPlugin> constructor;
					try {
						constructor = (Constructor<TonbotPlugin>) pluginClass.getConstructor(TonbotPluginArgs.class);
					} catch (NoSuchMethodException e) {
						LOG.error("Plugin '{}' must have a single argument constuctor that accepts a TonbotPluginArgs.",
								pluginClassName);
						return null;
					}

					TonbotPluginArgs pluginArgs = getPluginArgs(pluginClassName, prefix, discordClient);

					TonbotPlugin plugin;
					try {
						plugin = constructor.newInstance(pluginArgs);
					} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
						LOG.error("Unable to create an instance of plugin '{}'.", pluginClassName, e);
						return null;
					}

					return plugin;
				})
				.filter(plugin -> plugin != null)
				.collect(Collectors.toList());
	}

	private TonbotPluginArgs getPluginArgs(String pluginClassName, String prefix, IDiscordClient discordClient) {
		File configFile = new File(configDir + "plugin_config/" + pluginClassName + ".config");

		if (!configFile.exists()) {
			configFile = null;
		}

		TonbotPluginArgs pluginArgs = TonbotPluginArgs.builder()
				.discordClient(discordClient)
				.prefix(prefix)
				.configFile(configFile)
				.build();

		return pluginArgs;
	}
}
