package com.tonberry.tonbot.modules.coinflip;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.Prefix;

class CoinFlipModule extends AbstractModule {

    private final String prefix;

    public CoinFlipModule(String prefix) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
    }

    public void configure() {
        bind(String.class).annotatedWith(Prefix.class).toInstance(prefix);
    }

    @Provides
    @Singleton
    Plugin plugin(CoinFlipper coinFlipper) {
        return Plugin.builder()
                .name("Coin Flipper")
                .usageDescription("``" + prefix + " flip a coin`` Flips a coin")
                .eventListeners(ImmutableSet.of(coinFlipper))
                .build();
    }
}
