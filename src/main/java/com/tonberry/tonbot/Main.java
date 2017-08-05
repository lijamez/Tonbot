package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tonberry.tonbot.modules.coinflip.CoinFlipModule;
import com.tonberry.tonbot.modules.diagnostics.DiscordDiagnosticsModule;
import com.tonberry.tonbot.modules.tmdb.TMDbModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String discordBotToken = System.getProperty("discordBotToken");
        Preconditions.checkNotNull(discordBotToken, "discordBotToken system property must be set.");

        String tmdbApiKey = System.getProperty("tmdbApiKey");
        Preconditions.checkNotNull(tmdbApiKey, "tmdbApiKey system property must be set.");

        Injector injector = Guice.createInjector(
                new TonbotModule(discordBotToken, "t!"),
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
