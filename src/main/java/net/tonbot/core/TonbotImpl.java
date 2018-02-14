package net.tonbot.core;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import net.tonbot.common.Activity;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Prefix;
import net.tonbot.common.TonbotPlugin;
import net.tonbot.common.TonbotTechnicalFault;
import net.tonbot.core.permission.PermissionManager;
import net.tonbot.core.permission.PermissionPlugin;
import net.tonbot.core.request.RequestMapper;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

class TonbotImpl implements Tonbot {

	private static final Logger LOG = LoggerFactory.getLogger(TonbotImpl.class);

	private final IDiscordClient discordClient;
	private final PluginLoader pluginLoader;
	private final List<String> pluginFqns;
	private final String prefix;
	private final BotUtils botUtils;
	private final PlayingTextSetter playingTextSetter;
	private final Map<String, String> aliasToCanonicalRouteMap;
	private final Color color;
	private final ActivityPrinter activityPrinter;
	private final RequestMapper requestMapper;

	private List<TonbotPlugin> plugins;

	@Inject
	public TonbotImpl(
			final IDiscordClient discordClient,
			final PluginLoader pluginLoader,
			final List<String> pluginFqns,
			@Prefix final String prefix,
			final BotUtils botUtils,
			final PlayingTextSetter playingTextSetter,
			final Map<String, String> aliasToCanonicalRouteMap,
			final Color color,
			final ActivityPrinter activityPrinter,
			final RequestMapper requestMapper) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.pluginLoader = Preconditions.checkNotNull(pluginLoader, "pluginLoader must be non-null.");
		this.pluginFqns = Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.playingTextSetter = Preconditions.checkNotNull(playingTextSetter, "playingTextSetter must be non-null.");
		this.aliasToCanonicalRouteMap = Preconditions.checkNotNull(aliasToCanonicalRouteMap,
				"aliasToCanonicalRouteMap must be non-null.");
		this.color = Preconditions.checkNotNull(color, "color must be non-null.");
		this.activityPrinter = Preconditions.checkNotNull(activityPrinter, "activityPrinter must be non-null.");
		this.requestMapper = Preconditions.checkNotNull(requestMapper, "requestMapper must be non-null.");
	}

	public void run() {

		List<String> allPluginFqns = ImmutableList.<String>builder()
				.add(PermissionPlugin.class.getName())
				.addAll(pluginFqns)
				.build();

		// Standard Plugins
		this.plugins = pluginLoader.instantiatePlugins(allPluginFqns, prefix, discordClient, botUtils, color);

		PermissionPlugin permissionPlugin = this.plugins.stream()
				.filter(plugin -> plugin instanceof PermissionPlugin)
				.map(plugin -> (PermissionPlugin) plugin)
				.findFirst()
				.orElseThrow(() -> new TonbotTechnicalFault("Unable to load PermissionPlugin."));

		PermissionManager permissionManager = permissionPlugin.getPermissionManager();

		// Register Activities
		Set<Activity> activities = plugins.stream()
				.map(TonbotPlugin::getActivities)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());

		InfoActivity infoActivity = new InfoActivity(discordClient, botUtils, color);
		activities.add(infoActivity);

		Aliases aliases = new Aliases(aliasToCanonicalRouteMap, activities);

		HelpActivity helpActivity = new HelpActivity(
				activityPrinter,
				botUtils,
				prefix,
				plugins,
				permissionManager,
				aliases,
				color);
		permissionManager.addPublicActivity(helpActivity);
		activities.add(helpActivity);

		// Since we just added the help activity to the list of activities, we need to
		// updates the aliases.
		aliases.update(activities);

		activities.forEach(a -> LOG.debug("Registered {}", a.getClass().getName()));

		EventDispatcher eventDispatcher = new EventDispatcher(
				botUtils,
				prefix,
				activities,
				aliases,
				permissionManager,
				activityPrinter,
				requestMapper);

		discordClient.getDispatcher().registerListener(eventDispatcher);

		// Raw Listeners
		plugins.stream()
				.map(TonbotPlugin::getRawEventListeners)
				.flatMap(Collection::stream)
				.forEach(eventListener -> {
					LOG.debug("Registering raw listener {}", eventListener.getClass().getName());
					discordClient.getDispatcher().registerListener(eventListener);
				});

		try {
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
			LOG.error("Failed to connect to Discord.", e);
			throw e;
		}
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
