package net.tonbot.core;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import net.tonbot.common.BotUtils;
import net.tonbot.common.Prefix;
import sx.blah.discord.api.IDiscordClient;

class TonbotModule extends AbstractModule {

	private final String prefix;
	private final List<String> pluginFqns;
	private final IDiscordClient discordClient;
	private final String configDir;

	public TonbotModule(
			String prefix,
			List<String> pluginFqns,
			IDiscordClient discordClient,
			String configDir) {
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.pluginFqns = Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.configDir = Preconditions.checkNotNull(configDir, "configDir must be non-null.");
	}

	public void configure() {
		bind(Tonbot.class).to(TonbotImpl.class);
		bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
		bind(IDiscordClient.class).toInstance(discordClient);
		bind(String.class).annotatedWith(ConfigDir.class).toInstance(configDir);
		bind(BotUtils.class).to(BotUtilsImpl.class).in(Scopes.SINGLETON);
	}

	@Provides
	@Singleton
	List<String> pluginFqns() {
		return pluginFqns;
	}

	@Provides
	@Singleton
	PlayingTextSetter playingTextSetter() {
		// Every minute, set the playing text.
		return new PlayingTextSetter(discordClient, 60000, prefix);
	}
}
