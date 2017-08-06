package com.tonberry.tonbot.modules.tmdb;

import com.google.inject.Guice;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import com.tonberry.tonbot.common.TonbotPluginFactory;

public class TMDbPluginFactory implements TonbotPluginFactory {

    private TMDbModule module;

    public void initialize(TonbotPluginArgs args) {
        this.module = new TMDbModule(args.getPrefix());
    }

    public Plugin build() {
        return Guice.createInjector(module)
                .getInstance(Plugin.class);
    }
}
