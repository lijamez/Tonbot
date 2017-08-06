package com.tonberry.tonbot.modules.time;

import com.google.inject.Guice;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import com.tonberry.tonbot.common.TonbotPluginFactory;

public class TimePluginFactory implements TonbotPluginFactory {

    private TimeModule module;

    public void initialize(TonbotPluginArgs args) {
        this.module = new TimeModule(args.getPrefix());
    }

    public Plugin build() {
        return Guice.createInjector(module)
                .getInstance(Plugin.class);
    }
}
