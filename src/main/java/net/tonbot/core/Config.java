package net.tonbot.core;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Data;

@Data
class Config {

	private final String prefix;
	private final String discordBotToken;
	private final List<String> pluginNames;
	private final Map<String, String> aliases;

	/**
	 * Constructor.
	 * 
	 * @param prefix
	 *            The bot prefix. Non-null.
	 * @param discordBotToken
	 *            Discord bot token. Non-null.
	 * @param pluginNames
	 *            Fully qualified names to plugins. Non-null.
	 * @param aliases
	 *            A mapping from alias routes to canonical activity routes.
	 *            Non-null.
	 */
	@JsonCreator
	public Config(
			@JsonProperty("prefix") String prefix,
			@JsonProperty("discordBotToken") String discordBotToken,
			@JsonProperty("plugins") List<String> pluginNames,
			@JsonProperty("aliases") Map<String, String> aliases) {
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.discordBotToken = Preconditions.checkNotNull(discordBotToken, "discordBotToken must be non-null.");

		Preconditions.checkNotNull(pluginNames, "pluginNames must be non-null.");
		this.pluginNames = ImmutableList.copyOf(pluginNames);

		Preconditions.checkNotNull(aliases, "aliases must be non-null.");
		this.aliases = ImmutableMap.copyOf(aliases);
	}
}
