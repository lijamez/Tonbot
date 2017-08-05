package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tonberry.tonbot.modules.coinflip.CoinFlipModule;
import com.tonberry.tonbot.modules.diagnostics.DiscordDiagnosticsModule;
import com.tonberry.tonbot.modules.helloworld.HelloWorldModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String token = args[0];

        Preconditions.checkNotNull(token, "Please enter a token as the first argument.");

        Injector injector = Guice.createInjector(
                new TonbotModule(token),
                new DiscordDiagnosticsModule(),
                new HelloWorldModule(),
                new CoinFlipModule());
        Tonbot bot = injector.getInstance(Tonbot.class);

        try {
            bot.run();
        } catch (Exception e) {
            LOG.error("Tonbot died due to an uncaught exception. RIP.", e);
        }
    }
}
