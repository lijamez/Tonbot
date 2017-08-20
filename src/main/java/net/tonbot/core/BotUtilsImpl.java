package net.tonbot.core;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.common.BotUtils;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.RequestBuilder;

class BotUtilsImpl implements BotUtils {

	private final IDiscordClient discordClient;

	@Inject
	public BotUtilsImpl(IDiscordClient discordClient) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
	}

	@Override
	public void sendMessage(IChannel channel, String message) {
		Preconditions.checkNotNull(channel, "channel must be non-null.");
		Preconditions.checkNotNull(message, "message must be non-null.");

		new RequestBuilder(discordClient)
				.shouldBufferRequests(true)
				.setAsync(true)
				.doAction(() -> {
					channel.sendMessage(message);
					return true;
				})
				.execute();
	}

	@Override
	public void sendEmbed(IChannel channel, EmbedObject embedObj) {
		Preconditions.checkNotNull(channel, "channel must be non-null.");
		Preconditions.checkNotNull(embedObj, "embedObj must be non-null.");

		new RequestBuilder(discordClient)
				.shouldBufferRequests(true)
				.setAsync(true)
				.doAction(() -> {
					channel.sendMessage(embedObj);
					return true;
				})
				.execute();
	}
}
