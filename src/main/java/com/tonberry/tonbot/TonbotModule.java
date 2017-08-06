package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.Prefix;
import sx.blah.discord.api.IDiscordClient;

import java.util.List;

class TonbotModule extends AbstractModule {

    private final String token;
    private final String prefix;
    private final List<String> pluginFqns;
    private final IDiscordClient discordClient;

    public TonbotModule(String token, String prefix, List<String> pluginFqns, IDiscordClient discordClient) {
        this.token = Preconditions.checkNotNull(token, "token must be non-null.");
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.pluginFqns = Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
        this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
    }

    public void configure() {
        bind(Tonbot.class).to(TonbotImpl.class);
        bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
        bind(IDiscordClient.class).toInstance(discordClient);
    }

    @Provides
    @Singleton
    List<String> pluginFqns() {
        return pluginFqns;
    }
}
