package net.tonbot.core;

import java.awt.Color;

import org.apache.commons.lang3.SystemUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Enactable;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

class InfoActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("info")
			.description("Gets information about Tonbot.")
			.build();

	private static final Runtime RUNTIME = Runtime.getRuntime();
	private static final long BYTES_IN_MIB = 1048576;

	private final IDiscordClient discordClient;
	private final BotUtils botUtils;
	private final Color color;

	@Inject
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

	@Enactable
	public void enact(MessageReceivedEvent event) {
		EmbedBuilder eb = new EmbedBuilder();

		eb.withTitle("Tonbot");
		eb.withUrl("https://github.com/lijamez/Tonbot");
		eb.withThumbnail(discordClient.getOurUser().getAvatarURL());

		// Environment Info
		eb.appendField("Operating System",
				String.format("%s %s (%s)", SystemUtils.OS_NAME, SystemUtils.OS_VERSION, SystemUtils.OS_ARCH), false);
		eb.appendField("Runtime", SystemUtils.JAVA_RUNTIME_NAME + " " + SystemUtils.JAVA_RUNTIME_VERSION, false);

		eb.appendField("Discord Framework", Discord4J.NAME + " " + Discord4J.VERSION, false);

		// System Vitals
		eb.appendField("Number of Processors", RUNTIME.availableProcessors() + "", true);

		long maxMiB = RUNTIME.maxMemory() / BYTES_IN_MIB;
		long totalMiB = RUNTIME.totalMemory() / BYTES_IN_MIB;
		long freeMiB = RUNTIME.freeMemory() / BYTES_IN_MIB;
		long usedMiB = totalMiB - freeMiB;

		String mem = String.format("%d MiB / %d MiB (%.0f%%)\n%d MiB Max", usedMiB, totalMiB,
				(((double) usedMiB) / totalMiB) * 100, maxMiB);
		eb.appendField("Memory Usage", mem, true);

		// Discord Stats
		eb.appendField("Connected Servers", discordClient.getGuilds().size() + "", true);
		eb.appendField("Number of Shards", discordClient.getShardCount() + "", true);

		eb.withFooterText("Developed by Tonberry");
		eb.withColor(color);

		botUtils.sendEmbed(event.getChannel(), eb.build());
	}

}
