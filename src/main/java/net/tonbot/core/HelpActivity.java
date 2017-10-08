package net.tonbot.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;

import com.google.common.base.Preconditions;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Route;
import net.tonbot.common.TonbotPlugin;
import net.tonbot.core.permission.PermissionManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

class HelpActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("help")
			.build();

	private final BotUtils botUtils;
	private final String prefix;
	private final List<TonbotPlugin> plugins;
	private final PermissionManager permissionManager;

	public HelpActivity(
			BotUtils botUtils,
			String prefix,
			List<TonbotPlugin> plugins,
			PermissionManager permissionManager) {
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.plugins = Preconditions.checkNotNull(plugins, "plugins must be non-null.");
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Override
	public void enact(MessageReceivedEvent event, String args) {
		if (StringUtils.isBlank(args)) {
			printCommands(event.getAuthor(), event.getChannel(), event.getGuild());
		} else {
			printCommandHelp(event.getAuthor(), event.getChannel(), event.getGuild(), args);
		}
	}

	private void printCommandHelp(IUser user, IChannel channel, IGuild guild, String args) {
		Route referencedRoute = Route.from(args);

		if (referencedRoute.equals(this.getDescriptor().getRoute())) {
			botUtils.sendMessage(channel, "Very funny. :expressionless:");
			return;
		}

		Optional<Activity> optActivity = plugins.stream()
				.filter(plugin -> !plugin.isHidden())
				.flatMap(plugin -> plugin.getActivities().stream())
				.filter(activity -> referencedRoute.equals(activity.getDescriptor().getRoute()))
				.filter(activity -> permissionManager.checkAccessibility(activity, user, guild))
				.findFirst();

		if (optActivity.isPresent()) {
			Activity activity = optActivity.get();

			StringBuffer sb = new StringBuffer();

			sb.append("**Command:** ``")
					.append(activity.getDescriptor().getRoute())
					.append("``\n\n");

			// Display route aliases, if at least one exists
			List<Route> routeAliases = activity.getDescriptor().getRouteAliases();
			if (!routeAliases.isEmpty()) {
				sb.append("**Aliases:**\n");
				routeAliases.forEach(alias -> {
					sb.append("``")
							.append(StringUtils.join(alias, " "))
							.append("``\n");
				});

				sb.append("\n");
			}

			// Display usage description.
			Optional<String> usageDescription = activity.getDescriptor().getUsageDescription();
			if (usageDescription.isPresent()) {
				String usageDescriptionForDisplay = substitutePlaceholders(usageDescription.get(), referencedRoute);
				sb.append(usageDescriptionForDisplay);
			} else {
				sb.append("No additional usage information.");
			}

			String finalUsageMessage = sb.toString();
			botUtils.sendMessage(channel, finalUsageMessage);

		} else {
			botUtils.sendMessage(channel, "Sorry, that command doesn't exist.");
		}
	}

	private String substitutePlaceholders(String descWithPlaceholders, Route referencedRoute) {

		Map<String, Object> valueMap = new HashMap<>();
		valueMap.put("absoluteReferencedRoute", prefix + " " + referencedRoute);

		String result = StrSubstitutor.replace(descWithPlaceholders, valueMap);
		return result;
	}

	private void printCommands(IUser user, IChannel channel, IGuild guild) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.withDesc("Here's what I can do...");

		plugins.stream()
				.filter(plugin -> !plugin.isHidden())
				.forEach(plugin -> {
					StringBuffer sb = new StringBuffer();
					plugin.getActivities().stream()
							.filter(activity -> permissionManager.checkAccessibility(activity, user, guild))
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

		embedBuilder
				.withFooterText("You can also say '" + prefix + " help <command>' to get more help for that command.");
		botUtils.sendEmbed(channel, embedBuilder.build());
	}
}
