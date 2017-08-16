package net.tonbot.core;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Data;

@Data
class Config {

	private final String prefix;
	private final String discordBotToken;
	private final List<String> pluginNames;

	@JsonCreator
	public Config(
			@JsonProperty("prefix") String prefix,
			@JsonProperty("discordBotToken") String discordBotToken,
			@JsonProperty("plugins") List<String> pluginNames) {
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.discordBotToken = Preconditions.checkNotNull(discordBotToken, "discordBotToken must be non-null.");

		Preconditions.checkNotNull(pluginNames, "pluginNames must be non-null.");
		this.pluginNames = ImmutableList.copyOf(pluginNames);
	}
}
