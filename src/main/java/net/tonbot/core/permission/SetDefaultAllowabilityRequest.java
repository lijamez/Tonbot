package net.tonbot.core.permission;

import javax.annotation.Nonnull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.tonbot.common.Param;

@ToString
@EqualsAndHashCode
class SetDefaultAllowabilityRequest {

	@Getter
	@Param(name = "allow/deny", ordinal = 0, description = "Must be 'allow' or 'deny'.")
	@Nonnull
	private Allowability allowability;
}
