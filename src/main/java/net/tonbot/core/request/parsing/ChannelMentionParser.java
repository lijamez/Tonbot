package net.tonbot.core.request.parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.core.request.Context;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;

class ChannelMentionParser implements Parser {

	private static final Pattern CHANNEL_MENTION_PATTERN = Pattern.compile("^<#(\\d+)>");

	private final IDiscordClient discordClient;

	@Inject
	public ChannelMentionParser(IDiscordClient discordClient) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
	}

	@Override
	public boolean supports(Class<?> type) {
		Preconditions.checkNotNull(type, "type must be non-null.");
		return IChannel.class.isAssignableFrom(type);
	}

	@Override
	public ParseResult parse(String content, Class<?> targetType, Context context) {
		Preconditions.checkNotNull(content, "content must be non-null.");

		Matcher matcher = CHANNEL_MENTION_PATTERN.matcher(content);
		if (matcher.find()) {
			// Since the CHANNEL_MENTION_PATTERN must match at the beginning of the string,
			// we
			// can
			// assume that matcher.start() is 0.
			String remainingArgs = content.substring(matcher.end(), content.length());
			String matchedValue = matcher.group(1);

			long channelId = Long.parseLong(matchedValue);

			IChannel channel = discordClient.getChannelByID(channelId);

			return new ParseResult(channel, remainingArgs);
		} else {
			throw new ParseException(
					"Line parser encountered unexpected input which could not be parsed as a channel mention.");
		}
	}
}
