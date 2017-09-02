package net.tonbot.core.permission;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class GuildConfiguration {

	private final List<Rule> rules;
	private boolean defaultAllow;
}
