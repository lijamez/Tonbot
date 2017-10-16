package net.tonbot.core;

import java.awt.Color;

import org.apache.commons.lang3.SystemUtils;

import com.google.common.base.Preconditions;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

class InfoActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("info")
			.description("Gets information about Tonbot.")
			.build();

	private final IDiscordClient discordClient;
	private final BotUtils botUtils;
	private final Color color;

	public InfoActivity(
			IDiscordClient discordClient,
			BotUtils botUtils,
			Color color) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.color = Preconditions.checkNotNull(color, "color must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Override
	public void enact(MessageReceivedEvent event, String args) {
		EmbedBuilder eb = new EmbedBuilder();

		eb.withTitle("Tonbot");
		eb.withUrl("https://github.com/lijamez/Tonbot");
		eb.withThumbnail(discordClient.getOurUser().getAvatarURL());

		eb.appendField("Operating System",
				String.format("%s %s (%s)", SystemUtils.OS_NAME, SystemUtils.OS_VERSION, SystemUtils.OS_ARCH), false);
		eb.appendField("Runtime", SystemUtils.JAVA_RUNTIME_NAME + " " + SystemUtils.JAVA_RUNTIME_VERSION, false);
		eb.appendField("Discord Framework", Discord4J.NAME + " " + Discord4J.VERSION, false);

		eb.withFooterText("Developed by Tonberry");
		eb.withColor(color);

		botUtils.sendEmbed(event.getChannel(), eb.build());
	}

}