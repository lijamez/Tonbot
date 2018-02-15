package net.tonbot.core.request.parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.core.request.Context;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IRole;

class RoleMentionParser implements Parser {

	private static final Pattern ROLE_MENTION_PATTERN = Pattern.compile("^<@&(\\d+)>");
	private static final Pattern EVERYONE_PATTERN = Pattern.compile("^@everyone");

	private final IDiscordClient discordClient;

	@Inject
	public RoleMentionParser(IDiscordClient discordClient) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
	}

	@Override
	public boolean supports(Class<?> type) {
		Preconditions.checkNotNull(type, "type must be non-null.");
		return IRole.class.isAssignableFrom(type);
	}

	@Override
	public ParseResult parse(String content, Class<?> targetType, Context context) {
		Preconditions.checkNotNull(content, "content must be non-null.");
		Preconditions.checkNotNull(context, "context must be non-null.");

		ParseResult pr = parseStandardRoleMention(content);
		if (pr == null) {
			pr = parseEveryoneMention(content, context);
		}

		if (pr == null) {
			throw new ParseException(
					"Line parser encountered unexpected input which could not be parsed as a role mention.");
		}

		return pr;
	}

	private ParseResult parseStandardRoleMention(String content) {
		Matcher matcher = ROLE_MENTION_PATTERN.matcher(content);
		if (matcher.find()) {
			// Since the ROLE_MENTION_PATTERN must match at the beginning of the string, we
			// can
			// assume that matcher.start() is 0.
			String remainingArgs = content.substring(matcher.end(), content.length());
			String matchedValue = matcher.group(1);

			long roleId = Long.parseLong(matchedValue);

			IRole role = discordClient.getRoleByID(roleId);

			return new ParseResult(role, remainingArgs);
		}

		return null;
	}

	private ParseResult parseEveryoneMention(String content, Context context) {
		Matcher matcher = EVERYONE_PATTERN.matcher(content);
		if (matcher.find()) {
			// Since the EVERYONE_PATTERN must match at the beginning of the string, we can
			// assume that matcher.start() is 0.
			String remainingArgs = content.substring(matcher.end(), content.length());

			IRole role = context.getGuild().getEveryoneRole();

			return new ParseResult(role, remainingArgs);
		}

		return null;
	}
}
