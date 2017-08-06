package com.tonberry.tonbot.modules.coinflip;

import com.google.inject.Guice;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import com.tonberry.tonbot.common.TonbotPluginFactory;

public class CoinFlipPluginFactory implements TonbotPluginFactory {

    private CoinFlipModule module;

    public void initialize(TonbotPluginArgs args) {
        this.module = new CoinFlipModule(args.getPrefix());
    }

    public Plugin build() {
        return Guice.createInjector(module)
                .getInstance(Plugin.class);
    }
}
