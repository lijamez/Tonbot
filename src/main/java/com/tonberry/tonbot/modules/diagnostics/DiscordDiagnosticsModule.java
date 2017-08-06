package com.tonberry.tonbot.modules.diagnostics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.TonbotPluginModule;
import sx.blah.discord.api.IDiscordClient;

public class DiscordDiagnosticsModule extends TonbotPluginModule {

    public DiscordDiagnosticsModule(String prefix, IDiscordClient discordClient) {
        super(prefix, discordClient);
    }

    public void configure() {
        super.configure();

        bind(Plugin.class).toProvider(DiscordDiagnosticsModule.PluginProvider.class);
        expose(Plugin.class);
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
