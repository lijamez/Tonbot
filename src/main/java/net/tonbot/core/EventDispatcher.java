package net.tonbot.core;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import lombok.Data;
import lombok.NonNull;
import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.ActivityUsageException;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Route;
import net.tonbot.common.TonbotBusinessException;
import net.tonbot.core.permission.PermissionManager;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * Responsible for dispatching actions based on a message event and contents. A
 * set of activities is registered with this dispatcher. Each activity has a
 * route. This class will check the message event's contents, tokenize the
 * words, and attempt to find the best activity to run. The best activity is the
 * one that has the longest token prefix.
 */
class EventDispatcher {

	private static final Logger LOG = LoggerFactory.getLogger(EventDispatcher.class);

	private static final String TOKENIZATION_DELIMITER = " ";

	private final BotUtils botUtils;
	private final String prefix;
	private final Set<Activity> activities;
	private final Aliases aliases;
	private final PermissionManager permissionManager;
	private final ActivityPrinter activityPrinter;

	public EventDispatcher(
			BotUtils botUtils,
			String prefix,
			Set<Activity> activities,
			Aliases aliases,
			PermissionManager permissionManager,
			ActivityPrinter activityPrinter) {
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
		this.activityPrinter = Preconditions.checkNotNull(activityPrinter, "activityPrinter must be non-null.");

		Preconditions.checkNotNull(activities, "activities must be non-null.");
		this.activities = ImmutableSet.copyOf(activities);

		this.aliases = Preconditions.checkNotNull(aliases, "aliases must be non-null.");
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event) {
		String messageString = event.getMessage().getContent();

		if (!StringUtils.startsWith(messageString, prefix)) {
			return;
		}

		String messageWithoutPrefix = messageString.substring(prefix.length(), messageString.length()).trim();
		List<String> tokens = Arrays.asList(messageWithoutPrefix.split(TOKENIZATION_DELIMITER)).stream()
				.filter(token -> !StringUtils.isBlank(token))
				.collect(Collectors.toList());

		if (tokens.isEmpty()) {
			return;
		}

		ActivityMatch activityMatch = matchActivity(tokens);

		if (activityMatch == null) {
			return;
		}

		if (!permissionManager.checkAccessibility(activityMatch.getMatchedActivity(), event.getAuthor(),
				event.getGuild())) {
			LOG.debug("Activity '{}' was denied to user '{}' in guild '{}'",
					activityMatch.getMatchedActivity().getClass(),
					event.getAuthor(),
					event.getGuild().getName());
			return;
		}

		List<String> routePath = tokens.subList(0, activityMatch.getMatchedRoute().getPath().size());
		int routeChars = (routePath.size() * TOKENIZATION_DELIMITER.length()) + routePath.stream()
				.mapToInt(String::length)
				.sum();
		// The part of the message that doesn't contain the prefix or route.
		String remainingMessage;
		if (routeChars > messageWithoutPrefix.length()) {
			remainingMessage = "";
		} else {
			remainingMessage = messageWithoutPrefix.substring(routeChars, messageWithoutPrefix.length());
		}

		try {
			activityMatch.getMatchedActivity().enact(event, remainingMessage);
		} catch (ActivityUsageException e) {
			String usageMessage = new StringBuilder()
					.append(e.getMessage())
					.append("\n\n")
					.append("Usage:\n")
					.append(activityPrinter.getBasicUsage(
							activityMatch.getMatchedRoute(),
							activityMatch.getMatchedActivity().getDescriptor()))
					.toString();

			botUtils.sendMessage(event.getChannel(), usageMessage);
		} catch (TonbotBusinessException e) {
			botUtils.sendMessage(event.getChannel(), e.getMessage());
		} catch (Exception e) {
			botUtils.sendMessage(event.getChannel(), "Something bad happened. :confounded:");
			LOG.error("Uncaught exception received from activity.", e);
		}
	}

	private ActivityMatch matchActivity(List<String> remainingTokens) {
		// Find the activity to run. We will first match by the main route.
		Route preliminaryRoute = Route.from(remainingTokens);
		ActivityMatch matchedActivity = null;
		for (Activity activity : activities) {
			ActivityDescriptor descriptor = activity.getDescriptor();

			Route route = descriptor.getRoute();
			if (preliminaryRoute.isPrefixedBy(route)
					&& (matchedActivity == null
							|| matchedActivity.getMatchedRoute().getPath().size() < route.getPath().size())) {
				matchedActivity = new ActivityMatch(activity, route);
			}
		}

		if (matchedActivity == null) {
			// Since no activity was matched, we'll fall back matching via the route
			// aliases.
			matchedActivity = findBestActivityByRouteAlias(remainingTokens);
		}

		return matchedActivity;
	}

	private ActivityMatch findBestActivityByRouteAlias(List<String> remainingTokens) {
		Route preliminaryRoute = Route.from(remainingTokens);
		ActivityMatch match = null;

		for (Activity activity : aliases.getKnownActivities()) {
			List<Route> aliasesOfActivity = aliases.getAliasesOf(activity);
			for (Route alias : aliasesOfActivity) {
				if (preliminaryRoute.isPrefixedBy(alias)
						&& (match == null || match.getMatchedRoute().getPath().size() < alias.getPath().size())) {
					match = new ActivityMatch(activity, alias);
				}
			}
		}

		return match;
	}

	/**
	 * Object which represents a matched activity. The matchedRoute may be the
	 * canonical route or an alias.
	 */
	@Data
	private static class ActivityMatch {
		@NonNull
		private final Activity matchedActivity;

		@NonNull
		private final Route matchedRoute;
	}
}
