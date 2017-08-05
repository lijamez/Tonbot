package com.tonberry.tonbot.modules.coinflip;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.tonberry.tonbot.Plugin;

public class CoinFlipModule extends AbstractModule {

    public void configure() {
        Multibinder<Plugin> pluginBinder = Multibinder.newSetBinder(binder(), Plugin.class);
        pluginBinder.addBinding().toInstance(
                Plugin.builder()
                        .name("Coin Flipper")
                        .usageDescription("``t! flip a coin`` Flips a coin")
                        .eventListeners(ImmutableSet.of(new CoinFlipper()))
                        .build()
        );
    }
}
