package com.tonberry.tonbot.modules.diagnostics;

import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.tonberry.tonbot.common.PeriodicTask;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

public class DiscordDiagnosticsPlugin extends TonbotPlugin {

    private Injector injector;

    public DiscordDiagnosticsPlugin(TonbotPluginArgs args) {
        super(args);

        this.injector = Guice.createInjector(new DiscordDiagnosticsModule(args.getPrefix(), args.getDiscordClient()));
    }

    @Override
    public String getFriendlyName() {
        return "Discord Diagnostics Logger";
    }

    @Override
    public String getActionDescription() {
        return "Display diagnostic information";
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public Set<PeriodicTask> getPeriodicTasks() {
        return injector.getInstance(Key.get(new TypeLiteral<Set<PeriodicTask>>() {}));
    }
}
