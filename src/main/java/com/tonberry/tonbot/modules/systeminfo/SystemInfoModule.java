package com.tonberry.tonbot.modules.systeminfo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.Prefix;
import com.tonberry.tonbot.common.TonbotPluginModule;
import sx.blah.discord.api.IDiscordClient;

public class SystemInfoModule extends TonbotPluginModule {

    public SystemInfoModule(String prefix, IDiscordClient discordClient) {
        super(prefix, discordClient);
    }

    public void configure() {
        super.configure();

        bind(Plugin.class).toProvider(PluginProvider.class);
        expose(Plugin.class);
    }

    static class PluginProvider implements Provider<Plugin> {

        private final SystemInfoEventListener eventListener;
        private final String prefix;

        @Inject
        public PluginProvider(SystemInfoEventListener eventListener, @Prefix String prefix) {
            this.eventListener = Preconditions.checkNotNull(eventListener, "tmdbEventListener must be non-null.");
            this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        }

        public Plugin get() {
            return Plugin.builder()
                    .name("System Info")
                    .usageDescription("``" + prefix + " systeminfo``  Displays system information.")
                    .eventListeners(ImmutableSet.of(eventListener))
                    .build();
        }
    }
}
