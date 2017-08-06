package com.tonberry.tonbot.modules.diagnostics;

import com.google.inject.Guice;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

public class DiscordDiagnosticsPlugin implements TonbotPlugin {

    private DiscordDiagnosticsModule module;

    public void initialize(TonbotPluginArgs args) {
        this.module = new DiscordDiagnosticsModule(args.getPrefix(), args.getDiscordClient());
    }

    public PluginResources build() {
        return Guice.createInjector(module)
                .getInstance(PluginResources.class);
    }
}
