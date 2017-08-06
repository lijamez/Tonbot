package com.tonberry.tonbot.modules.diagnostics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.Prefix;
import sx.blah.discord.api.IDiscordClient;

class DiscordDiagnosticsModule extends AbstractModule {

    private static final long PERIOD_MS = 300000;

    private final String prefix;
    private final IDiscordClient discordClient;

    public DiscordDiagnosticsModule(String prefix, IDiscordClient discordClient) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null");
        this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null");
    }

    public void configure() {
        bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
        bind(IDiscordClient.class).toInstance(discordClient);
    }

    @Provides
    @Singleton
    PluginResources plugin(IDiscordClient discordClient) {
        DiscordDiagnosticsLogger diagnosticsLogger = new DiscordDiagnosticsLogger(discordClient, PERIOD_MS);

        return PluginResources.builder()
                .name("Discord Diagnostics Logger")
                .usageDescription("")
                .hidden(true)
                .periodicTasks(ImmutableSet.of(diagnosticsLogger))
                .build();
    }
}
