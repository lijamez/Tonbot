package net.tonbot.core;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import net.tonbot.common.TonbotPlugin;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

class TonbotImpl implements Tonbot {

	private static final Logger LOG = LoggerFactory.getLogger(TonbotImpl.class);

	private final IDiscordClient discordClient;
	private final PlayingTextSetter playingTextSetter;
	private final List<TonbotPlugin> plugins;
	private final EventDispatcher eventDispatcher;

	@Inject
	public TonbotImpl(final IDiscordClient discordClient, final PlayingTextSetter playingTextSetter,
			final List<TonbotPlugin> plugins, final EventDispatcher eventDispatcher) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.playingTextSetter = Preconditions.checkNotNull(playingTextSetter, "playingTextSetter must be non-null.");
		this.plugins = Preconditions.checkNotNull(plugins, "plugins must be non-null.");
		this.eventDispatcher = Preconditions.checkNotNull(eventDispatcher, "eventDispatcher must be non-null.");
	}

	public void run() {

		discordClient.getDispatcher().registerListener(eventDispatcher);

		// Raw Listeners
		plugins.stream().map(TonbotPlugin::getRawEventListeners).flatMap(Collection::stream).forEach(eventListener -> {
			LOG.debug("Registering raw listener {}", eventListener.getClass().getName());
			discordClient.getDispatcher().registerListener(eventListener);
		});

		try {
			discordClient.login();

			LOG.info("Waiting for Discord API readiness...");

			while (!discordClient.isReady()) {
			}

			LOG.info("Discord API is ready.");

			plugins.stream().map(TonbotPlugin::getPeriodicTasks).flatMap(Collection::stream).forEach(periodicTask -> {
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
