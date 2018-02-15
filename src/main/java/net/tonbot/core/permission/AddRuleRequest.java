package net.tonbot.core.permission;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.tonbot.common.Param;
import sx.blah.discord.handle.obj.IRole;

@ToString
@EqualsAndHashCode
public class AddRuleRequest {

	@Getter
	private int displayIndex;

	@Param(name = "index", ordinal = 0, description = "The position to insert the rule at.")
	public void setDisplayIndex(@Nonnull Integer value) {
		Preconditions.checkArgument(value > 0, "index must be a positive number.");

		this.displayIndex = value;
	}

	@Getter
	@Param(name = "role", ordinal = 1, description = "A role mention.")
	@Nonnull
	private IRole role;

	@Getter
	@Param(name = "allow/deny", ordinal = 2, description = "To allow or deny access to the given role.")
	@Nonnull
	private Allowability allowability;

	@Getter
	@Param(name = "route path expression", ordinal = 3, description = "A path to the command.")
	@Nonnull
	private String path;
}
