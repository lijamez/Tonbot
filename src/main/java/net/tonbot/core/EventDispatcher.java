package net.tonbot.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
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
import net.tonbot.common.Enactable;
import net.tonbot.common.Route;
import net.tonbot.common.TonbotBusinessException;
import net.tonbot.core.permission.PermissionManager;
import net.tonbot.core.request.Context;
import net.tonbot.core.request.RequestMapper;
import net.tonbot.core.request.RequestMappingException;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

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
	private final RequestMapper requestMapper;

	public EventDispatcher(
			BotUtils botUtils,
			String prefix,
			Set<Activity> activities,
			Aliases aliases,
			PermissionManager permissionManager,
			ActivityPrinter activityPrinter,
			RequestMapper requestMapper) {
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
		this.activityPrinter = Preconditions.checkNotNull(activityPrinter, "activityPrinter must be non-null.");
		this.requestMapper = Preconditions.checkNotNull(requestMapper, "requestMapper must be non-null.");

		Preconditions.checkNotNull(activities, "activities must be non-null.");
		this.activities = ImmutableSet.copyOf(activities);

		this.aliases = Preconditions.checkNotNull(aliases, "aliases must be non-null.");
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event) {

		// Ignore bots
		if (event.getAuthor().isBot()) {
			return;
		}

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

		LOG.info("Activity enacted by message.\n"
				+ "Author: {} (ID {})\n"
				+ "Guild: {} (ID {})\n"
				+ "Message: {} (ID: {})\n"
				+ "Matched Activity: {}",
				event.getAuthor().getName(),
				event.getAuthor().getLongID(),
				event.getGuild().getName(),
				event.getGuild().getLongID(),
				event.getMessage().getContent(),
				event.getMessage().getLongID(),
				activityMatch.getMatchedActivity().getClass().getName());

		enactActivity(activityMatch, event, remainingMessage);
	}

	private void enactActivity(ActivityMatch activityMatch, MessageReceivedEvent event, String args) {

		Long latency = Long.MIN_VALUE;

		try {
			long start = System.nanoTime();
			try {
				Runnable runnable = getEnactableMethod(activityMatch, event, args);
				runnable.run();
			} finally {
				latency = System.nanoTime() - start;
			}
		} catch (ActivityUsageException e) {
			sendUsageMessage(e.getMessage(), activityMatch.getMatchedRoute(),
					activityMatch.getMatchedActivity().getDescriptor(), event.getChannel());
		} catch (TonbotBusinessException e) {
			botUtils.sendMessage(event.getChannel(), e.getMessage());
		} catch (Exception e) {
			botUtils.sendMessage(event.getChannel(), "Something bad happened. :confounded:");
			LOG.error("Uncaught exception received from activity.", e);
		} finally {
			LOG.info("Activity {} latency: {} ms",
					activityMatch.getMatchedActivity().getClass().getName(),
					latency / 1_000_000);
		}
	}

	private Runnable getEnactableMethod(ActivityMatch activityMatch, MessageReceivedEvent event, String args) {

		Runnable enactableMethod = getShinyEnactableMethod(activityMatch, event, args);

		if (enactableMethod == null) {
			return getLegacyEnactableMethod(activityMatch, event, args);
		} else {
			return enactableMethod;
		}
	}

	private Runnable getLegacyEnactableMethod(ActivityMatch activityMatch, MessageReceivedEvent event, String args) {
		Activity activity = activityMatch.getMatchedActivity();

		return () -> {
			activity.enact(event, args);
		};
	}

	private Runnable getShinyEnactableMethod(ActivityMatch activityMatch, MessageReceivedEvent event, String args) {
		Activity activity = activityMatch.getMatchedActivity();

		List<Method> enactableMethods = MethodUtils.getMethodsListWithAnnotation(activity.getClass(), Enactable.class);
		if (enactableMethods.size() > 1) {
			throw new IllegalStateException(
					"Activity " + activity.getClass() + " must have at most 1 method with @Enactable annotation.");
		}

		if (enactableMethods.isEmpty()) {
			return null;
		}

		Method enactableMethod = enactableMethods.get(0);
		// Check that this method takes in two arguments: MessageReceivedEvent and some
		// Object
		Class<?>[] parameterTypes = enactableMethod.getParameterTypes();
		if (parameterTypes.length < 1 || parameterTypes.length > 2) {
			throw new IllegalStateException("Activity " + activity.getClass()
					+ " method annotated with @Enactable must have 1 or 2 parameters. The first being a MessageReceivedEvent (required) and the second being any mappable object (if applicable).");
		}

		if (!(MessageReceivedEvent.class.isAssignableFrom(parameterTypes[0]))) {
			throw new IllegalStateException("Activity " + activity.getClass()
					+ " method annotated with @Enactable must have the first parameter be a MessageReceivedEvent.");
		}

		Object requestObj;

		if (parameterTypes.length == 2) {
			Context context = new Context(event.getGuild());
			Class<?> requestType = parameterTypes[1];

			try {
				requestObj = requestMapper.map(args, requestType, context);
			} catch (RequestMappingException e) {
				throw new ActivityUsageException(e.getMessage(), e);
			}

		} else {
			requestObj = null;
		}

		enactableMethod.setAccessible(true);

		return () -> {
			try {
				if (requestObj == null) {
					enactableMethod.invoke(activity, event);
				} else {
					enactableMethod.invoke(activity, event, requestObj);
				}

			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(
						"Unable to invoke Enactable method " + enactableMethod + " of class " + activity.getClass(),
						e);
			}
		};
	}

	private ActivityMatch matchActivity(List<String> remainingTokens) {
		// Find the activity to run. We will first match by the main route.
		Route preliminaryRoute = Route.from(remainingTokens);

		// Route alias matching takes precedence of natural route matching.
		ActivityMatch matchedActivity = findBestActivityByRouteAlias(preliminaryRoute);

		if (matchedActivity == null) {
			// Since no activity was matched, we'll fall back matching via the natural
			// routes.
			matchedActivity = findBestActivityByNaturalRoute(preliminaryRoute);
		}

		return matchedActivity;
	}

	private ActivityMatch findBestActivityByRouteAlias(Route preliminaryRoute) {
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

	private ActivityMatch findBestActivityByNaturalRoute(Route preliminaryRoute) {
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

		return matchedActivity;
	}

	private void sendUsageMessage(String errorMessage, Route route, ActivityDescriptor descriptor, IChannel channel) {
		String usageMessage = new StringBuilder()
				.append(errorMessage)
				.append("\n\n")
				.append("Usage:\n")
				.append(activityPrinter.getBasicUsage(
						route,
						descriptor))
				.toString();

		botUtils.sendMessage(channel, usageMessage);
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
