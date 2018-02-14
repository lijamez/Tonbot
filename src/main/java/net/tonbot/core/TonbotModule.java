package net.tonbot.core;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import net.tonbot.common.BotUtils;
import net.tonbot.common.Prefix;
import net.tonbot.core.request.parsing.ParserModule;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

class TonbotModule extends AbstractModule {

	private final String botUserToken;
	private final String prefix;
	private final List<String> pluginFqns;
	private final String configDir;
	private final Map<String, String> aliasToCanonicalRoutes;
	private final Color color;

	/**
	 * Constructor.
	 * 
	 * @param botUserToken
	 *            The Discord bot token. Non-null.
	 * @param prefix
	 *            The message prefix. Messages sent with this prefix to a channel
	 *            that Tonbot can read will be sent to the {@link EventDispatcher}.
	 *            Non-null.
	 * @param pluginFqns
	 *            A list of fully qualified plugin class names to be loaded.
	 *            Non-null.
	 * @param configDir
	 *            The configuration directory. Non-null.
	 * @param aliasToCanonicalRoutes
	 *            A map of command alias routes to canonical routes. Non-null.
	 * @param color
	 *            A color to be used by the plugins. Non-null.
	 */
	public TonbotModule(
			String botUserToken,
			String prefix,
			List<String> pluginFqns,
			String configDir,
			Map<String, String> aliasToCanonicalRoutes,
			Color color) {
		this.botUserToken = Preconditions.checkNotNull(botUserToken, "botUserToken must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.pluginFqns = Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
		this.configDir = Preconditions.checkNotNull(configDir, "configDir must be non-null.");
		this.aliasToCanonicalRoutes = Preconditions.checkNotNull(aliasToCanonicalRoutes,
				"aliasToCanonicalRoutes must be non-null.");
		this.color = Preconditions.checkNotNull(color, "color must be non-null.");
	}

	public void configure() {
		bind(Tonbot.class).to(TonbotImpl.class);
		bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
		bind(String.class).annotatedWith(ConfigDir.class).toInstance(configDir);
		bind(BotUtils.class).to(BotUtilsImpl.class).in(Scopes.SINGLETON);
		bind(new TypeLiteral<Map<String, String>>() {
		}).toInstance(aliasToCanonicalRoutes);
		bind(Color.class).toInstance(color);

		install(new ParserModule());
	}

	@Provides
	@Singleton
	List<String> pluginFqns() {
		return pluginFqns;
	}

	@Provides
	@Singleton
	PlayingTextSetter playingTextSetter(IDiscordClient discordClient) {
		// Every minute, set the playing text.
		return new PlayingTextSetter(discordClient, 60000, prefix);
	}

	@Provides
	@Singleton
	IDiscordClient discordClient(RejectedExecutionHandlerImpl rejectedExecHandler) {

		// Creates what is basically a fixed thread pool with this number of threads.
		int threadCount = Runtime.getRuntime().availableProcessors() * 4;

		IDiscordClient discordClient = new ClientBuilder()
				.withToken(botUserToken)
				.withRecommendedShardCount()
				.setMaxReconnectAttempts(100)
				.withMinimumDispatchThreads(threadCount)
				.withMaximumDispatchThreads(threadCount)
				.withEventOverflowCapacity(20)
				.withEventBackpressureHandler(rejectedExecHandler)
				.build();

		return discordClient;
	}
}
