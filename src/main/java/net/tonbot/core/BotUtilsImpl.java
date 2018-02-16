package net.tonbot.core;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.common.BotUtils;
import net.tonbot.common.TonbotTechnicalFault;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;
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

		new RequestBuilder(discordClient).shouldBufferRequests(true).setAsync(true).doAction(() -> {
			channel.sendMessage(message);
			return true;
		}).execute();
	}

	@Override
	public IMessage sendMessageSync(IChannel channel, String message) {
		Preconditions.checkNotNull(channel, "channel must be non-null.");
		Preconditions.checkNotNull(message, "message must be non-null.");

		Future<IMessage> sentMessage = RequestBuffer.request(() -> {
			return channel.sendMessage(message);
		});

		try {
			return sentMessage.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new TonbotTechnicalFault("Unable to send message.", e);
		}
	}

	@Override
	public void sendEmbed(IChannel channel, EmbedObject embedObj) {
		Preconditions.checkNotNull(channel, "channel must be non-null.");
		Preconditions.checkNotNull(embedObj, "embedObj must be non-null.");

		new RequestBuilder(discordClient).shouldBufferRequests(true).setAsync(true).doAction(() -> {
			channel.sendMessage(embedObj);
			return true;
		}).execute();
	}

	@Override
	public void sendEmbed(IChannel channel, EmbedObject embedObj, InputStream imageFileStream, String fileName) {
		Preconditions.checkNotNull(channel, "channel must be non-null.");
		Preconditions.checkNotNull(embedObj, "embedObj must be non-null.");
		Preconditions.checkNotNull(imageFileStream, "imageFileStream must be non-null.");
		Preconditions.checkNotNull(fileName, "fileName must be non-null.");

		new RequestBuilder(discordClient).shouldBufferRequests(true).setAsync(true).doAction(() -> {
			channel.sendFile(embedObj, imageFileStream, fileName);
			return true;
		}).execute();
	}
}
