package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.tonberry.tonbot.common.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;

class TonbotImpl implements Tonbot {

    private static final Logger LOG = LoggerFactory.getLogger(TonbotImpl.class);

    private final IDiscordClient discordClient;
    private final Set<Plugin> plugins;
    private final HelpHandler helpHandler;

    @Inject
    public TonbotImpl(
            final IDiscordClient discordClient,
            final Set<Plugin> plugins,
            final HelpHandler helpHandler) {
        this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
        this.plugins = Preconditions.checkNotNull(plugins, "plugins must be non-null.");
        this.helpHandler = Preconditions.checkNotNull(helpHandler, "helpHandler must be non-null.");
    }

    public void run() {
        try {
            printPluginsInfo();

            LOG.info("Registering listeners...");
            plugins.stream()
                    .map(Plugin::getEventListeners)
                    .flatMap(Collection::stream)
                    .forEach(eventListener -> {
                        discordClient.getDispatcher().registerListener(eventListener);
                        LOG.info("Registered event listener '{}'", eventListener.getClass().getName());
                    });
            discordClient.getDispatcher().registerListener(helpHandler);

            discordClient.login();

            LOG.info("Waiting for Discord API readiness...");

            while (!discordClient.isReady()) { }

            LOG.info("Discord API is ready.");

            plugins.stream()
                    .map(Plugin::getPeriodicTasks)
                    .flatMap(Collection::stream)
                    .forEach(periodicTask -> {
                        periodicTask.start();
                        LOG.info("Periodic task '{}' has started.", periodicTask.getClass().getName());
                    });

            LOG.info("Tonbot is online!");
        } catch (DiscordException e) {
            LOG.error("Failed to start Tonbot.", e);
            throw e;
        }
    }

    private void printPluginsInfo() {
        final StringBuffer pluginsSb = new StringBuffer();
        pluginsSb.append(plugins.size());
        pluginsSb.append(" plugins found: \n");
        plugins.forEach(plugin -> {
            pluginsSb.append(plugin.getName());
            pluginsSb.append("\n");
        });
        LOG.info(pluginsSb.toString());
    }
}
