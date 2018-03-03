package net.tonbot.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import lombok.Data;
import lombok.NonNull;
import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.ActivityUsageException;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Enactable;
import net.tonbot.common.Prefix;
import net.tonbot.common.Route;
import net.tonbot.common.TonbotBusinessException;
import net.tonbot.common.TonbotException;
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
	private static final long ERROR_TTL = 15000;
	private static final TimeUnit ERROR_TTL_UNIT = TimeUnit.MILLISECONDS;

	private static final String TOKENIZATION_DELIMITER = " ";

	private final BotUtils botUtils;
	private final String prefix;

	private final PermissionManager permissionManager;
	private final ActivityPrinter activityPrinter;
	private final RequestMapper requestMapper;
	private final ActivityMatcher activityMatcher;

	@Inject
	public EventDispatcher(BotUtils botUtils, @Prefix String prefix, Set<Activity> activities, Aliases aliases,
			PermissionManager permissionManager, ActivityPrinter activityPrinter, RequestMapper requestMapper,
			ActivityMatcher activityMatcher) {
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.permissionManager = Preconditions.checkNotNull(permissionManager, "permissionManager must be non-null.");
		this.activityPrinter = Preconditions.checkNotNull(activityPrinter, "activityPrinter must be non-null.");
		this.requestMapper = Preconditions.checkNotNull(requestMapper, "requestMapper must be non-null.");
		this.activityMatcher = Preconditions.checkNotNull(activityMatcher, "activityMatcher must be non-null.");
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

		ActivityMatch activityMatch = activityMatcher.matchActivity(tokens).orElse(null);

		if (activityMatch == null) {
			return;
		}

		if (!permissionManager.checkAccessibility(activityMatch.getMatchedActivity(), event.getAuthor(),
				event.getGuild())) {
			LOG.debug("Activity '{}' was denied to user '{}' in guild '{}'",
					activityMatch.getMatchedActivity().getClass(), event.getAuthor(), event.getGuild().getName());
			return;
		}

		List<String> routePath = tokens.subList(0, activityMatch.getMatchedRoute().getPath().size());
		int routeChars = (routePath.size() * TOKENIZATION_DELIMITER.length())
				+ routePath.stream().mapToInt(String::length).sum();

		// remainingMessage is the part of the message that doesn't contain the prefix
		// or route.
		String remainingMessage;
		if (routeChars > messageWithoutPrefix.length()) {
			remainingMessage = "";
		} else {
			remainingMessage = messageWithoutPrefix.substring(routeChars, messageWithoutPrefix.length());
		}

		LOG.info(
				"Activity enacted by message.\n" + "Author: {} (ID {})\n" + "Guild: {} (ID {})\n"
						+ "Message: {} (ID: {})\n" + "Matched Activity: {}",
				event.getAuthor().getName(), event.getAuthor().getLongID(), event.getGuild().getName(),
				event.getGuild().getLongID(), event.getMessage().getContent(), event.getMessage().getLongID(),
				activityMatch.getMatchedActivity().getClass().getName());

		enactActivity(activityMatch, event, remainingMessage);
	}

	private void enactActivity(ActivityMatch activityMatch, MessageReceivedEvent event, String args) {

		Long latency = Long.MIN_VALUE;
		
		EnactableMethod enactableMethod = null;
		try {
			enactableMethod = getEnactableMethod(activityMatch.getMatchedActivity(), event, args);
			
			long start = System.nanoTime();
			try {
				enactableMethod.getRunnable().run();
			} finally {
				latency = System.nanoTime() - start;
			}
		} catch (ActivityUsageException e) {
			sendUsageMessage(e.getMessage(), activityMatch.getMatchedRoute(),
					activityMatch.getMatchedActivity().getDescriptor(), event.getChannel());
		} catch (TonbotBusinessException e) {
			botUtils.sendMessage(event.getChannel(), e.getMessage(), ERROR_TTL, ERROR_TTL_UNIT);
		} catch (Exception e) {
			botUtils.sendMessage(event.getChannel(), "Something bad happened. :confounded:", ERROR_TTL, ERROR_TTL_UNIT);
			LOG.error("Uncaught exception received from activity.", e);
		} finally {
			LOG.info("Activity {} latency: {} ms", activityMatch.getMatchedActivity().getClass().getName(),
					latency / 1_000_000);
			
			if (enactableMethod != null && enactableMethod.getEnactableAnnotation().deleteCommand()) {
				botUtils.deleteMessagesQuietly(event.getMessage());
			}
		}
	}

	private EnactableMethod getEnactableMethod(Activity activity, MessageReceivedEvent event, String args) {

		List<Method> annotatedMethods = MethodUtils.getMethodsListWithAnnotation(activity.getClass(), Enactable.class);
		if (annotatedMethods.size() != 1) {
			throw new IllegalStateException(
					"Activity " + activity.getClass() + " must have exactly 1 method with @Enactable annotation.");
		}

		Method annotatedMethod = annotatedMethods.get(0);
		// Check that this method takes in two arguments: MessageReceivedEvent and some
		// Object
		Class<?>[] parameterTypes = annotatedMethod.getParameterTypes();
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
			Type requestType = annotatedMethod.getGenericParameterTypes()[1];

			Class<?> requestClass;
			if (requestType instanceof Class) {
				requestClass = parameterTypes[1];
			} else {
				try {
					requestClass = activity.getRequestType();
				} catch (NotImplementedException e) {
					throw new IllegalStateException(
							"The activity's getRequestType() method must return a requestType if the @Enactable method uses a generic type for its request.");
				}
			}

			Context context = new Context(event.getGuild());

			try {
				requestObj = requestMapper.map(args, requestClass, context);
				LOG.info("Request: {}", requestObj);
			} catch (RequestMappingException e) {
				throw new ActivityUsageException(e.getMessage(), e);
			}

		} else {
			requestObj = null;
		}

		annotatedMethod.setAccessible(true);

		Enactable enactableAnnotation = annotatedMethod.getAnnotation(Enactable.class);
		
		Runnable runnable = () -> {
			try {
				if (requestObj == null) {
					annotatedMethod.invoke(activity, event);
				} else {
					annotatedMethod.invoke(activity, event, requestObj);
				}

			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new RuntimeException(
						"Unable to invoke Enactable method " + annotatedMethod + " of class " + activity.getClass(), e);
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
				if (cause instanceof TonbotException) {
					throw (TonbotException) cause;
				} else {
					throw new RuntimeException(
							"Unable to invoke Enactable method " + annotatedMethod + " of class " + activity.getClass(),
							e);
				}
			}
		};
		
		return new EnactableMethod(enactableAnnotation, runnable);
	}

	private void sendUsageMessage(String errorMessage, Route route, ActivityDescriptor descriptor, IChannel channel) {
		String usageMessage = new StringBuilder().append(errorMessage).append("\n\n").append("Usage:\n")
				.append(activityPrinter.getBasicUsage(route, descriptor)).toString();

		botUtils.sendMessage(channel, usageMessage);
	}
	
	@Data
	private class EnactableMethod {
		
		@NonNull
		Enactable enactableAnnotation;
		
		@NonNull
		Runnable runnable;
	}
}
