package com.tonberry.tonbot.modules.diagnostics;

import com.google.inject.Guice;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import com.tonberry.tonbot.common.TonbotPluginFactory;

public class DiscordDiagnosticsPluginFactory implements TonbotPluginFactory {

    private DiscordDiagnosticsModule module;

    public void initialize(TonbotPluginArgs args) {
        this.module = new DiscordDiagnosticsModule(args.getPrefix(), args.getDiscordClient());
    }

    public Plugin build() {
        return Guice.createInjector(module)
                .getInstance(Plugin.class);
    }
}
