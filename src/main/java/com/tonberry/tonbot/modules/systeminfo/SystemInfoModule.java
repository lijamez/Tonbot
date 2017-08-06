package com.tonberry.tonbot.modules.systeminfo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.Prefix;

public class SystemInfoModule extends AbstractModule {

    public void configure() {
        Multibinder<Plugin> pluginBinder = Multibinder.newSetBinder(binder(), Plugin.class);
        pluginBinder.addBinding().toProvider(SystemInfoModule.PluginProvider.class);
    }

    static class PluginProvider implements Provider<Plugin> {

        private final SystemInfoEventListener eventListener;
        private final String prefix;

        @Inject
        public PluginProvider(SystemInfoEventListener eventListener, @Prefix String prefix) {
            this.eventListener = Preconditions.checkNotNull(eventListener, "tmdbEventListener must be non-null.");
            this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        }

        public Plugin get() {
            return Plugin.builder()
                    .name("System Info")
                    .usageDescription("``" + prefix + " systeminfo``  Displays system information.")
                    .eventListeners(ImmutableSet.of(eventListener))
                    .build();
        }
    }
}
