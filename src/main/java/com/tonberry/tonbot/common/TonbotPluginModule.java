package com.tonberry.tonbot.common;

import com.google.common.base.Preconditions;
import com.google.inject.PrivateModule;
import sx.blah.discord.api.IDiscordClient;

public abstract class TonbotPluginModule extends PrivateModule {

    private String prefix;
    private IDiscordClient discordClient;

    public TonbotPluginModule(String prefix, IDiscordClient discordClient) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
    }

    protected void configure() {
        bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
        bind(IDiscordClient.class).toInstance(discordClient);
    }

    protected String getPrefix() {
        return prefix;
    }
}
