package com.tonberry.tonbot.modules.systeminfo;

import com.google.inject.Guice;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import com.tonberry.tonbot.common.TonbotPluginFactory;

public class SystemInfoPluginFactory implements TonbotPluginFactory {

    private SystemInfoModule module;

    public void initialize(TonbotPluginArgs args) {
        this.module = new SystemInfoModule(args.getPrefix());
    }

    public Plugin build() {
        return Guice.createInjector(module)
                .getInstance(Plugin.class);
    }
}
