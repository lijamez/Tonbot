package net.tonbot.core.permission;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.tonbot.core.request.Param;
import sx.blah.discord.handle.obj.IRole;

@ToString
@EqualsAndHashCode
public class AddRuleRequest {

	@Getter
	private int displayIndex;

	@Param(name = "index", ordinal = 0)
	public void setDisplayIndex(@Nonnull Integer value) {
		Preconditions.checkArgument(value > 0, "index must be a positive number.");

		this.displayIndex = value;
	}

	@Getter
	@Param(name = "role", ordinal = 1)
	@Nonnull
	private IRole role;

	@Getter
	@Param(name = "allow/deny", ordinal = 2)
	@Nonnull
	private Allowability allowability;

	@Getter
	@Param(name = "route path expression", ordinal = 3)
	@Nonnull
	private String path;
}
