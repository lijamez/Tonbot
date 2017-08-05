package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;

import java.util.Timer;
import java.util.TimerTask;

public abstract class PeriodicTask {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final Timer timer;
    private final long periodMs;
    private final IDiscordClient discordClient;

    public PeriodicTask(IDiscordClient discordClient, long periodMs) {
        this.timer = new Timer();

        this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");

        Preconditions.checkArgument(periodMs > 0, "periodMs must be greater than 0.");
        this.periodMs = periodMs;
    }

    public void start() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    performTask();
                } catch (Exception e) {
                    LOG.error("Unable to emit diagnostics.", e);
                }

            }
        };

        timer.scheduleAtFixedRate(task, 0, periodMs);
    }

    protected IDiscordClient getDiscordClient() {
        return discordClient;
    }

    protected abstract void performTask();
}
