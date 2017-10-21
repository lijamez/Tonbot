package net.tonbot.core;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.common.BotUtils;
import net.tonbot.common.PluginSetupException;
import net.tonbot.common.TonbotPlugin;
import net.tonbot.common.TonbotPluginArgs;
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
	 * @param botUtils
	 *            {@link BotUtils}. Non-null.
	 * @return A list of {@link PluginResources}s.
	 */
	@SuppressWarnings("unchecked")
	public List<TonbotPlugin> instantiatePlugins(
			List<String> pluginFqns,
			String prefix,
			IDiscordClient discordClient,
			BotUtils botUtils,
			Color color) {

		Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
		Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		Preconditions.checkNotNull(color, "color must be non-null.");

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
						LOG.warn(
								"Plugin '{}' is not valid. It must have a single argument constuctor that accepts a TonbotPluginArgs.",
								pluginClassName);
						return null;
					}

					TonbotPluginArgs pluginArgs = getPluginArgs(pluginClassName, prefix, discordClient, botUtils,
							color);

					TonbotPlugin plugin;
					try {
						plugin = constructor.newInstance(pluginArgs);
					} catch (InvocationTargetException e) {
						Throwable cause = e.getCause();
						if (cause instanceof PluginSetupException) {
							LOG.warn("Plugin {} is not set up or is set up incorrectly.",
									pluginClassName, cause);
							return null;
						} else {
							LOG.warn("Unable to create an instance of plugin '{}'.", pluginClassName, e);
							return null;
						}
					} catch (InstantiationException | IllegalAccessException e) {
						LOG.warn("Unable to create an instance of plugin '{}'.", pluginClassName, e);
						return null;
					}

					LOG.info("Loaded plugin: {}", plugin.getClass().getName());
					return plugin;
				})
				.filter(plugin -> plugin != null)
				.collect(Collectors.toList());
	}

	private TonbotPluginArgs getPluginArgs(
			String pluginClassName,
			String prefix,
			IDiscordClient discordClient,
			BotUtils botUtils,
			Color color) {

		File configFile = new File(configDir + "/plugins/" + pluginClassName + "/config.json");
		File pluginDataDir = new File(configDir + "/plugins/" + pluginClassName + "/data");

		TonbotPluginArgs pluginArgs = TonbotPluginArgs.builder()
				.discordClient(discordClient)
				.prefix(prefix)
				.configFile(configFile)
				.pluginDataDir(pluginDataDir)
				.botUtils(botUtils)
				.color(color)
				.build();

		return pluginArgs;
	}
}
