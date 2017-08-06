package com.tonberry.tonbot.modules.coinflip;

import com.google.inject.Guice;
import com.tonberry.tonbot.common.PluginResources;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import com.tonberry.tonbot.common.TonbotPlugin;

public class CoinFlipPlugin implements TonbotPlugin {

    private CoinFlipModule module;

    public void initialize(TonbotPluginArgs args) {
        this.module = new CoinFlipModule(args.getPrefix());
    }

    public PluginResources build() {
        return Guice.createInjector(module)
                .getInstance(PluginResources.class);
    }
}
