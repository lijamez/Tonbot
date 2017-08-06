package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.Prefix;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

class TonbotImpl implements Tonbot {

    private static final Logger LOG = LoggerFactory.getLogger(TonbotImpl.class);

    private final IDiscordClient discordClient;
    private final PluginLoader pluginLoader;
    private final List<String> pluginFqns;
    private final String prefix;

    @Inject
    public TonbotImpl(
            final IDiscordClient discordClient,
            final PluginLoader pluginLoader,
            final List<String> pluginFqns,
            @Prefix final String prefix) {
        this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
        this.pluginLoader = Preconditions.checkNotNull(pluginLoader, "pluginLoader must be non-null.");
        this.pluginFqns = Preconditions.checkNotNull(pluginFqns, "pluginFqns must be non-null.");
        this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
    }

    public void run() {
        try {
            TonbotPluginArgs pluginArgs = TonbotPluginArgs.builder()
                    .discordClient(discordClient)
                    .prefix(prefix)
                    .build();

            List<Plugin> plugins = pluginLoader.instantiatePlugins(pluginFqns, pluginArgs);

            printPluginsInfo(plugins);

            LOG.info("Registering listeners...");
            plugins.stream()
                    .map(Plugin::getEventListeners)
                    .flatMap(Collection::stream)
                    .forEach(eventListener -> {
                        discordClient.getDispatcher().registerListener(eventListener);
                        LOG.info("Registered event listener '{}'", eventListener.getClass().getName());
                    });

            HelpHandler helpHandler = new HelpHandler(prefix, plugins);
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

            discordClient.changePlayingText("say: " + helpHandler.getTrigger());
        } catch (DiscordException e) {
            LOG.error("Failed to start Tonbot.", e);
            throw e;
        }
    }

    private void printPluginsInfo(List<Plugin> plugins) {
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
