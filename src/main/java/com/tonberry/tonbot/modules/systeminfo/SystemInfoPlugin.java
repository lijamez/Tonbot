package com.tonberry.tonbot.modules.systeminfo;

import com.google.inject.Guice;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

public class SystemInfoPlugin implements TonbotPlugin {

    private SystemInfoModule module;

    public void initialize(TonbotPluginArgs args) {
        this.module = new SystemInfoModule(args.getPrefix());
    }

    public PluginResources build() {
        return Guice.createInjector(module)
                .getInstance(PluginResources.class);
    }
}
