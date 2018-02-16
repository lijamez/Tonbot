package net.tonbot.core;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Data;

@Data
class Config {

	private static final int HEX_RADIX = 16;

	private final String prefix;
	private final String discordBotToken;
	private final List<String> pluginNames;
	private final Map<String, String> aliases;
	private final Color color;

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
	public Config(@JsonProperty("prefix") String prefix, @JsonProperty("discordBotToken") String discordBotToken,
			@JsonProperty("plugins") List<String> pluginNames, @JsonProperty("aliases") Map<String, String> aliases,
			@JsonProperty("color") String rgb) {
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
		this.discordBotToken = Preconditions.checkNotNull(discordBotToken, "discordBotToken must be non-null.");

		Preconditions.checkNotNull(pluginNames, "pluginNames must be non-null.");
		this.pluginNames = ImmutableList.copyOf(pluginNames);

		Preconditions.checkNotNull(aliases, "aliases must be non-null.");
		this.aliases = ImmutableMap.copyOf(aliases);

		Preconditions.checkNotNull(rgb, "rgb must be non-null.");
		this.color = parseColorRgb(rgb);
	}

	private Color parseColorRgb(String rgb) {
		Preconditions.checkArgument(rgb.length() == 6, "Color RGB must be 6 characters.");

		String rHex = StringUtils.substring(rgb, 0, 2);
		String gHex = StringUtils.substring(rgb, 2, 4);
		String bHex = StringUtils.substring(rgb, 4, 6);

		try {
			int r = Integer.parseInt(rHex, HEX_RADIX);
			int g = Integer.parseInt(gHex, HEX_RADIX);
			int b = Integer.parseInt(bHex, HEX_RADIX);

			if (r > 255 || g > 255 || b > 255) {
				throw new IllegalArgumentException("Invalid color.");
			}

			return new Color(r, g, b);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid color.");
		}

	}
}
