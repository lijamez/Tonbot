package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.Prefix;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

class TonbotModule extends AbstractModule {

    private final String token;
    private final String prefix;

    public TonbotModule(String token, String prefix) {
        this.token = Preconditions.checkNotNull(token, "token must be non-null.");
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
    }

    public void configure() {
        bind(Tonbot.class).to(TonbotImpl.class);
        bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);

        Multibinder.newSetBinder(binder(), Plugin.class);
    }

    @Provides
    @Singleton
    IDiscordClient discordClient() {
        return new ClientBuilder()
                .withToken(token)
                .build();
    }
}
