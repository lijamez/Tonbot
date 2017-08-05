package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tonberry.tonbot.modules.coinflip.CoinFlipModule;
import com.tonberry.tonbot.modules.diagnostics.DiscordDiagnosticsModule;
import com.tonberry.tonbot.modules.tmdb.TMDbModule;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String DEFAULT_PREFIX = "t!";

    public static void main(String[] args) {
        String discordBotToken = System.getProperty("discordBotToken");
        Preconditions.checkNotNull(discordBotToken, "discordBotToken system property must be set.");

        String tmdbApiKey = System.getProperty("tmdbApiKey");
        Preconditions.checkNotNull(tmdbApiKey, "tmdbApiKey system property must be set.");

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

        Injector injector = Guice.createInjector(
                new TonbotModule(discordBotToken, prefix),
                new DiscordDiagnosticsModule(),
                new CoinFlipModule(),
                new TMDbModule(tmdbApiKey));

        Tonbot bot = injector.getInstance(Tonbot.class);

        try {
            bot.run();
        } catch (Exception e) {
            LOG.error("Tonbot died due to an uncaught exception. RIP.", e);
        }
    }
}
