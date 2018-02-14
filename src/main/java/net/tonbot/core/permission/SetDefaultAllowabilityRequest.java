package net.tonbot.core.permission;

import javax.annotation.Nonnull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.tonbot.core.request.Param;

@ToString
@EqualsAndHashCode
public class SetDefaultAllowabilityRequest {

	@Getter
	@Param(name = "allow/deny", ordinal = 0)
	@Nonnull
	private Allowability allowability;
}
