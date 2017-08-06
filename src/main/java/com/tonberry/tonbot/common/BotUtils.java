package com.tonberry.tonbot.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class BotUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BotUtils.class);

    public static void sendMessage(IChannel channel, String message) {
        RequestBuffer.request(() -> {
            try {
                channel.sendMessage(message);
            } catch (DiscordException e) {
                LOG.error("Message could not be sent.", e);
            }
        });
    }

    public static void sendEmbeddedContent(IChannel channel, EmbedObject embedObject) {
        RequestBuffer.request(() -> {
            try {
                channel.sendMessage(embedObject);
            } catch (DiscordException e) {
                LOG.error("Embedded object could not be sent.", e);
            }
        });
    }
}
