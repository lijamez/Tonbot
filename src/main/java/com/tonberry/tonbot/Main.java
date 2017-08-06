package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.TonbotPluginModule;
import com.tonberry.tonbot.modules.coinflip.CoinFlipModule;
import com.tonberry.tonbot.modules.diagnostics.DiscordDiagnosticsModule;
import com.tonberry.tonbot.modules.systeminfo.SystemInfoModule;
import com.tonberry.tonbot.modules.time.TimeModule;
import com.tonberry.tonbot.modules.tmdb.TMDbModule;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String DEFAULT_PREFIX = "t!";

    public static void main(String[] args) {
        String discordBotToken = System.getProperty("discordBotToken");
        Preconditions.checkNotNull(discordBotToken, "discordBotToken system property must be set.");

        CommandLineParser parser = new DefaultParser();

        // Read CLI options
        Options cliOptions = new Options();
        cliOptions.addOption("p","prefix", true, "specify the prefix which the bot will respond to");

        CommandLine cmd = null;
        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException e) {
            LOG.error("Unable to parse command line arguments.", e);
            System.exit(1);
        }

        String prefix = cmd.getOptionValue("prefix");
        if (prefix == null) {
            prefix = DEFAULT_PREFIX;
        }

        IDiscordClient discordClient = new ClientBuilder()
                .withToken(discordBotToken)
                .build();

        List<TonbotPluginModule> pluginModules = ImmutableList.of(
                new DiscordDiagnosticsModule(prefix, discordClient),
                new CoinFlipModule(prefix, discordClient),
                new TMDbModule(prefix, discordClient),
                new TimeModule(prefix, discordClient),
                new SystemInfoModule(prefix, discordClient));

        Set<Plugin> plugins = pluginModules.stream()
                .map(pluginModule -> {
                    try {
                        return Guice.createInjector(pluginModule).getInstance(Plugin.class);
                    } catch (ConfigurationException e) {
                        LOG.warn("Couldn't get a Plugin from " + pluginModule + ". Skipping it.", e);
                        return null;
                    }
                })
                .filter(plugin -> plugin != null)
                .collect(Collectors.toSet());


        Injector injector = Guice.createInjector(
                new TonbotModule(discordBotToken, prefix, plugins, discordClient));

        Tonbot bot = injector.getInstance(Tonbot.class);

        try {
            bot.run();
        } catch (Exception e) {
            LOG.error("Tonbot died due to an uncaught exception. RIP.", e);
        }
    }
}
