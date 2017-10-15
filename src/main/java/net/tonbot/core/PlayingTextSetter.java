package net.tonbot.core;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.common.PeriodicTask;
import net.tonbot.common.Prefix;
import sx.blah.discord.api.IDiscordClient;

/**
 * It's possible that the current playing text can expire. Hence, it should be
 * set periodically.
 */
class PlayingTextSetter extends PeriodicTask {

	private final String prefix;
	private final IDiscordClient discordClient;

	@Inject
	public PlayingTextSetter(IDiscordClient discordClient, long periodMs, @Prefix String prefix) {
		super(periodMs);
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
	}

	@Override
	protected void performTask() {
		discordClient.changePlayingText("say: " + prefix + "help");
	}
}
