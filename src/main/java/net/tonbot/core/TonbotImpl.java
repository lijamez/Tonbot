package net.tonbot.core;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import net.tonbot.common.Activity;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Prefix;
import net.tonbot.common.TonbotPlugin;
import net.tonbot.common.TonbotPluginArgs;
import net.tonbot.core.permission.PermissionManager;
import net.tonbot.core.permission.PermissionPlugin;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

class TonbotImpl implements Tonbot {

	private static final Logger LOG = LoggerFactory.getLogger(TonbotImpl.class);

	private final IDiscordClient discordClient;
	private final PluginLoader pluginLoader;
	private final List<String> pluginFqns;
	private final String prefix;
	private final String configDir;
	private final BotUtils botUtils;
	private final PlayingTextSetter playingTextSetter;

	private List<TonbotPlugin> plugins;

	@Inject
	public TonbotImpl(
			final IDiscordClient discordClient,
			final PluginLoader pluginLoader,
			final List<String> pluginFqns,
			@Prefix final String prefix,
			@ConfigDir final String configDir,
			final BotUtils botUtils,
			final PlayingTextSetter playingTextSetter) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.pluginLoader = Preconditions.checkNotNull(pluginLoader, "pluginLoader must be non-null.");
		this.pluginFqns = Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.configDir = Preconditions.checkNotNull(configDir, "configDir must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.playingTextSetter = Preconditions.checkNotNull(playingTextSetter, "playingTextSetter must be non-null.");
	}

	public void run() {
		try {
			// Standard Plugins
			this.plugins = pluginLoader.instantiatePlugins(pluginFqns, prefix, discordClient, botUtils);

			// System Plugins
			// TODO: This method of creating a plugin is a little janky. Maybe let the
			// plugin loader do it.
			PermissionPlugin permissionPlugin = new PermissionPlugin(
					TonbotPluginArgs.builder()
							.botUtils(botUtils)
							.prefix(prefix)
							.discordClient(discordClient)
							.configFile(new File(configDir + "/permissions.conf"))
							.build());
			plugins.add(permissionPlugin);

			printPluginsInfo(plugins);

			PermissionManager permissionManager = permissionPlugin.getPermissionManager();

			LOG.info("Registering activities...");
			Set<Activity> activities = plugins.stream()
					.map(TonbotPlugin::getActivities)
					.flatMap(Collection::stream)
					.collect(Collectors.toSet());

			HelpActivity helpActivity = new HelpActivity(botUtils, prefix, plugins, permissionManager);
			permissionManager.addPublicActivity(helpActivity);
			activities.add(helpActivity);

			EventDispatcher eventDispatcher = new EventDispatcher(botUtils, prefix, activities, permissionManager);

			discordClient.getDispatcher().registerListener(eventDispatcher);

			// Raw Listeners
			plugins.stream()
					.map(TonbotPlugin::getRawEventListeners)
					.flatMap(Collection::stream)
					.forEach(eventListener -> discordClient.getDispatcher().registerListener(eventListener));

			discordClient.login();

			LOG.info("Waiting for Discord API readiness...");

			while (!discordClient.isReady()) {
			}

			LOG.info("Discord API is ready.");

			plugins.stream()
					.map(TonbotPlugin::getPeriodicTasks)
					.flatMap(Collection::stream)
					.forEach(periodicTask -> {
						periodicTask.start();
						LOG.info("Periodic task '{}' has started.", periodicTask.getClass().getName());
					});

			playingTextSetter.start();

			LOG.info("Tonbot is online!");
		} catch (DiscordException e) {
			LOG.error("Failed to start Tonbot.", e);
			throw e;
		}
	}

	private void printPluginsInfo(List<TonbotPlugin> plugins) {
		final StringBuffer pluginsSb = new StringBuffer();
		pluginsSb.append(plugins.size());
		pluginsSb.append(" Plugins found: \n");
		plugins.forEach(plugin -> {
			pluginsSb.append(plugin.getFriendlyName());
			pluginsSb.append("\n");
		});
		LOG.info(pluginsSb.toString());
	}

	@Override
	public void destroy() {
		plugins.forEach(plugin -> {
			try {
				plugin.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
