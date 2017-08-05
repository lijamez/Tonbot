package com.tonberry.tonbot.modules.helloworld;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.tonberry.tonbot.Plugin;

public class HelloWorldModule extends AbstractModule {

    public void configure() {
        Multibinder<Plugin> pluginBinder = Multibinder.newSetBinder(binder(), Plugin.class);
        pluginBinder.addBinding().toInstance(
                Plugin.builder()
                        .name("Hello World Responder")
                        .eventListeners(ImmutableSet.of(new HelloWorldEventListener()))
                        .build()
        );
    }
}
