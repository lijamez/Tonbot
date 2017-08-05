package com.tonberry.tonbot.modules.coinflip;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.tonberry.tonbot.Plugin;
import com.tonberry.tonbot.Prefix;

public class CoinFlipModule extends AbstractModule {

    public void configure() {
        Multibinder<Plugin> pluginBinder = Multibinder.newSetBinder(binder(), Plugin.class);
        pluginBinder.addBinding().toProvider(PluginProvider.class);
    }

    static class PluginProvider implements Provider<Plugin> {

        private final String prefix;
        private final CoinFlipper coinFlipper;

        @Inject
        public PluginProvider(@Prefix String prefix, CoinFlipper coinFlipper) {
            this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
            this.coinFlipper = Preconditions.checkNotNull(coinFlipper, "coinFlipper must be non-null.");
        }

        public Plugin get() {
            return Plugin.builder()
                    .name("Coin Flipper")
                    .usageDescription("``" + prefix + " flip a coin`` Flips a coin")
                    .eventListeners(ImmutableSet.of(coinFlipper))
                    .build();
        }
    }
}
