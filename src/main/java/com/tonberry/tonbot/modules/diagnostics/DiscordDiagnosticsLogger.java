package com.tonberry.tonbot.modules.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tonberry.tonbot.common.PeriodicTask;

import sx.blah.discord.api.IDiscordClient;

class DiscordDiagnosticsLogger extends PeriodicTask {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordDiagnosticsLogger.class);

    public DiscordDiagnosticsLogger(IDiscordClient discordClient, long periodMs) {
        super(discordClient, periodMs);
    }

    @Override
    public void performTask() {
        IDiscordClient discordClient = this.getDiscordClient();
        LOG.info("Currently in " + discordClient.getGuilds().size() + " guilds.");
        LOG.info("Is ready? " + discordClient.isReady());
        LOG.info("Is logged in? " + discordClient.isLoggedIn());
    }
}
