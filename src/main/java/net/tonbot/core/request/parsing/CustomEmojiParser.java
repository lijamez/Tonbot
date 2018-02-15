package net.tonbot.core.request.parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import net.tonbot.core.request.Context;
import sx.blah.discord.handle.obj.IEmoji;

/**
 * Can parse custom emojis. Supports both static and animated emojis.
 */
class CustomEmojiParser implements Parser {

	private static final Pattern CUSTOM_EMOJI_PATTERN = Pattern.compile("<a?:[A-Za-z0-9_]{2,}:(\\d+)>");

	@Override
	public boolean supports(Class<?> type) {
		Preconditions.checkNotNull(type, "type must be non-null.");
		return IEmoji.class.isAssignableFrom(type);
	}

	@Override
	public ParseResult parse(String content, Class<?> targetType, Context context) {
		Preconditions.checkNotNull(content, "content must be non-null.");

		Matcher matcher = CUSTOM_EMOJI_PATTERN.matcher(content);
		if (matcher.find()) {
			// Since the CUSTOM_EMOJI_PATTERN must match at the beginning of the string, we
			// can
			// assume that matcher.start() is 0.
			String remainingArgs = content.substring(matcher.end(), content.length());
			String matchedValue = matcher.group(1);

			long emojiId = Long.parseLong(matchedValue);

			// Warning: emoji may be null if the custom emoji doesn't belong to the
			// context's guild!
			IEmoji emoji = context.getGuild().getEmojiByID(emojiId);

			return new ParseResult(emoji, remainingArgs);
		} else {
			throw new ParseException(
					"Line parser encountered unexpected input which could not be parsed as a custom emoji.");
		}
	}
}
