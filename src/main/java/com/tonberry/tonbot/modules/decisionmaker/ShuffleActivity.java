package com.tonberry.tonbot.modules.decisionmaker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.tonberry.tonbot.common.Activity;
import com.tonberry.tonbot.common.ActivityDescriptor;
import com.tonberry.tonbot.common.BotUtils;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

class ShuffleActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route(ImmutableList.of("shuffle"))
			.parameters(ImmutableList.of("list of items"))
			.description("Randomizes a comma separated list.")
			.build();

	private static final String DELIMITER = ",";

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Override
	public void enact(MessageReceivedEvent messageReceivedEvent, String args) {
		List<String> items = Arrays.asList(StringUtils.split(args, DELIMITER))
				.stream()
				.map(item -> StringUtils.trim(item))
				.collect(Collectors.toList());

		Collections.shuffle(items);

		String resultMessage = StringUtils.join(items, "\n");

		BotUtils.sendMessage(messageReceivedEvent.getChannel(), resultMessage);
	}
}
