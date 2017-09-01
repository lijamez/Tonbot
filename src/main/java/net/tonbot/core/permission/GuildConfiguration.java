package net.tonbot.core.permission;

import java.util.List;

import lombok.Data;

@Data
class GuildConfiguration {

	private final List<Rule> rules;
	private final boolean defaultAllow;
}
