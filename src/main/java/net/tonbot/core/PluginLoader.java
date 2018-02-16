package net.tonbot.core;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.common.BotUtils;
import net.tonbot.common.PluginSetupException;
import net.tonbot.common.TonbotPlugin;
import net.tonbot.common.TonbotPluginArgs;
import net.tonbot.common.TonbotTechnicalFault;
import sx.blah.discord.api.IDiscordClient;

class PluginLoader {

	private final String configDir;

	@Inject
	public PluginLoader(@ConfigDir String configDir) {
		this.configDir = Preconditions.checkNotNull(configDir, "configDir must be non-null.");
	}

	/**
	 * Instantiates plugins based on the fully qualified names of their
	 * {@link TonbotPlugin}.
	 *
	 * @param pluginFqns
	 *            List of fully qualified {@link TonbotPlugin} class names.
	 *            Non-null.
	 * @param prefix
	 *            The prefix. Non-null.
	 * @param discordClient
	 *            The discord client. Non-null.
	 * @param botUtils
	 *            {@link BotUtils}. Non-null.
	 * @return A list of {@link TonbotPlugin}s.
	 * @throws TonbotTechnicalFault
	 *             if a plugin could not be instantiated.
	 */
	public List<TonbotPlugin> instantiatePlugins(List<String> pluginFqns, String prefix, IDiscordClient discordClient,
			BotUtils botUtils, Color color) {

		Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
		Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		Preconditions.checkNotNull(color, "color must be non-null.");

		return pluginFqns.stream().map(String::trim)
				.map(pluginClassName -> instantiatePlugin(pluginClassName, prefix, discordClient, botUtils, color))
				.filter(plugin -> plugin != null).collect(Collectors.toList());
	}

	/**
	 * Instantiates a plugin based on its fully qualified name.
	 *
	 * @param pluginClassName
	 *            The fully qualified {@link TonbotPlugin} class name. Non-null.
	 * @param prefix
	 *            The prefix. Non-null.
	 * @param discordClient
	 *            The discord client. Non-null.
	 * @param botUtils
	 *            {@link BotUtils}. Non-null.
	 * @return The instantiated {@link TonbotPlugin}. Null if it failed.
	 * @throws TonbotTechnicalFault
	 *             if the plugin could not be instantiated.
	 */
	@SuppressWarnings("unchecked")
	public TonbotPlugin instantiatePlugin(String pluginClassName, String prefix, IDiscordClient discordClient,
			BotUtils botUtils, Color color) {
		Preconditions.checkNotNull(pluginClassName, "pluginClassName must be non-null.");
		Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		Preconditions.checkNotNull(color, "color must be non-null.");

		Class<?> pluginClass;
		try {
			pluginClass = Class.forName(pluginClassName);
		} catch (ClassNotFoundException e) {
			throw new TonbotTechnicalFault(
					"Plugin '" + pluginClassName + "' could not be loaded because it was not found on the classpath.",
					e);
		}

		if (!TonbotPlugin.class.isAssignableFrom(pluginClass)) {
			throw new TonbotTechnicalFault(
					"PluginResources '" + pluginClass + "' could not be loaded because it is not a TonbotPlugin.");
		}

		Constructor<TonbotPlugin> constructor;
		try {
			constructor = (Constructor<TonbotPlugin>) pluginClass.getConstructor(TonbotPluginArgs.class);
		} catch (NoSuchMethodException e) {
			throw new TonbotTechnicalFault("Plugin '" + pluginClassName
					+ "' is not valid. It must have a single argument constuctor that accepts a TonbotPluginArgs.");
		}

		TonbotPluginArgs pluginArgs = getPluginArgs(pluginClassName, prefix, discordClient, botUtils, color);

		TonbotPlugin plugin;
		try {
			plugin = constructor.newInstance(pluginArgs);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof PluginSetupException) {
				throw new TonbotTechnicalFault(
						"Plugin '" + pluginClassName + "' is not set up or is set up incorrectly.", cause);
			} else {
				throw new TonbotTechnicalFault("Unable to create an instance of plugin '" + pluginClassName + "'.",
						cause);
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new TonbotTechnicalFault("Unable to create an instance of plugin '" + pluginClassName + "'.", e);
		}

		return plugin;
	}

	private TonbotPluginArgs getPluginArgs(String pluginClassName, String prefix, IDiscordClient discordClient,
			BotUtils botUtils, Color color) {

		File configFile = new File(configDir + "/plugins/" + pluginClassName + "/config.json");
		File pluginDataDir = new File(configDir + "/plugins/" + pluginClassName + "/data");

		TonbotPluginArgs pluginArgs = TonbotPluginArgs.builder().discordClient(discordClient).prefix(prefix)
				.configFile(configFile).pluginDataDir(pluginDataDir).botUtils(botUtils).color(color).build();

		return pluginArgs;
	}
}
