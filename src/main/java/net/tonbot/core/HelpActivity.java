package net.tonbot.core;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.TonbotPlugin;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

class HelpActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route(ImmutableList.of("help"))
			.build();

	private final BotUtils botUtils;
	private final String prefix;
	private final List<TonbotPlugin> plugins;

	public HelpActivity(BotUtils botUtils, String prefix, List<TonbotPlugin> plugins) {
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.plugins = Preconditions.checkNotNull(plugins, "plugins must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Override
	public void enact(MessageReceivedEvent event, String args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.withDesc("Here's what I can do...");

		plugins.stream()
				.filter(plugin -> !plugin.isHidden())
				.forEach(plugin -> {
					StringBuffer sb = new StringBuffer();
					plugin.getActivities().stream()
							.map(Activity::getDescriptor)
							.forEach(activity -> {
								sb.append("``");
								sb.append(prefix);
								sb.append(" ");
								sb.append(StringUtils.join(activity.getRoute(), " "));
								sb.append(" ");

								List<String> formattedParams = activity.getParameters().stream()
										.map(param -> "<" + param + ">")
										.collect(Collectors.toList());

								sb.append(StringUtils.join(formattedParams, " "));

								sb.append("``");
								sb.append("    ");
								sb.append(activity.getDescription());
								sb.append("\n");
							});
					
					String description = sb.toString();
					
					if (!description.isEmpty()) {
						embedBuilder.appendField(plugin.getActionDescription(), sb.toString(), false);
					}
				});
		
		botUtils.sendEmbed(event.getChannel(), embedBuilder.build());
	}
}
