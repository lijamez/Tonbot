package net.tonbot.core.permission;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Data;

@Data
class GuildConfiguration {

	private final List<Rule> rules;
	private boolean defaultAllow;

	@JsonCreator
	GuildConfiguration(@JsonProperty("rules") List<Rule> rules, @JsonProperty("defaultAllow") boolean defaultAllow) {
		this.rules = Preconditions.checkNotNull(rules, "rules must be non-null.");
		this.defaultAllow = defaultAllow;
	}
}
