package net.tonbot.core.request.parsing;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ParserModule extends AbstractModule {

	@Override
	protected void configure() {

	}

	@Provides
	@Singleton
	List<Parser> parsers(IntegerParser intParser, LongParser longParser, ShortParser shortParser,
			FloatParser floatParser, DoubleParser doubleParser, StringParser stringParser, EnumParser enumParser,
			RoleMentionParser roleMentionParser, UserMentionParser userMentionParser,
			ChannelMentionParser channelMentionParser, CustomEmojiParser customEmojiParser) {
		return ImmutableList.of(intParser, longParser, shortParser, floatParser, doubleParser, stringParser, enumParser,
				roleMentionParser, userMentionParser, channelMentionParser, customEmojiParser);
	}

}
