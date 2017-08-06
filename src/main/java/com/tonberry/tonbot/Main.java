package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tonberry.tonbot.common.Plugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

import java.util.List;

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

        List<String> pluginFqns = ImmutableList.of(
                "com.tonberry.tonbot.modules.coinflip.CoinFlipPluginFactory",
                "com.tonberry.tonbot.modules.diagnostics.DiscordDiagnosticsPluginFactory",
                "com.tonberry.tonbot.modules.systeminfo.SystemInfoPluginFactory",
                "com.tonberry.tonbot.modules.time.TimePluginFactory",
                "com.tonberry.tonbot.modules.tmdb.TMDbPluginFactory"
        );

        Injector injector = Guice.createInjector(
                new TonbotModule(discordBotToken, prefix, pluginFqns, discordClient));

        Tonbot bot = injector.getInstance(Tonbot.class);

        try {
            bot.run();
        } catch (Exception e) {
            LOG.error("Tonbot died due to an uncaught exception. RIP.", e);
        }
    }
}
