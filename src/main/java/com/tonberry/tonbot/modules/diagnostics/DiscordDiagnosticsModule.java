package com.tonberry.tonbot.modules.diagnostics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.tonberry.tonbot.common.Plugin;
import sx.blah.discord.api.IDiscordClient;

public class DiscordDiagnosticsModule extends AbstractModule {

    public void configure() {
        Multibinder<Plugin> pluginBinder = Multibinder.newSetBinder(binder(), Plugin.class);
        pluginBinder.addBinding().toProvider(DiscordDiagnosticsModule.PluginProvider.class);
    }

    public static class PluginProvider implements Provider<Plugin> {

        private final IDiscordClient discordClient;

        @Inject
        public PluginProvider(IDiscordClient discordClient) {
            this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
        }

        public Plugin get() {
            DiscordDiagnosticsLogger diagnosticsLogger = new DiscordDiagnosticsLogger(discordClient, 30000);

            return Plugin.builder()
                    .name("Discord Diagnostics Logger")
                    .usageDescription("")
                    .hidden(true)
                    .periodicTasks(ImmutableSet.of(diagnosticsLogger))
                    .build();
        }
    }
}
