package net.tonbot.core;

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
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

class TonbotModule extends AbstractModule {

	private final String botUserToken;
	private final String prefix;
	private final List<String> pluginFqns;
	private final String configDir;
	private final Map<String, String> aliasToCanonicalRoutes;

	public TonbotModule(
			String botUserToken,
			String prefix,
			List<String> pluginFqns,
			String configDir,
			Map<String, String> aliasToCanonicalRoutes) {
		this.botUserToken = Preconditions.checkNotNull(botUserToken, "botUserToken must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.pluginFqns = Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
		this.configDir = Preconditions.checkNotNull(configDir, "configDir must be non-null.");
		this.aliasToCanonicalRoutes = Preconditions.checkNotNull(aliasToCanonicalRoutes,
				"aliasToCanonicalRoutes must be non-null.");
	}

	public void configure() {
		bind(Tonbot.class).to(TonbotImpl.class);
		bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
		bind(String.class).annotatedWith(ConfigDir.class).toInstance(configDir);
		bind(BotUtils.class).to(BotUtilsImpl.class).in(Scopes.SINGLETON);
		bind(new TypeLiteral<Map<String, String>>() {
		}).toInstance(aliasToCanonicalRoutes);
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
	IDiscordClient discordClient() {
		IDiscordClient discordClient = new ClientBuilder()
				.withToken(botUserToken)
				.withRecommendedShardCount()
				.setMaxReconnectAttempts(100)
				.setDaemon(true)
				.build();

		return discordClient;
	}
}
