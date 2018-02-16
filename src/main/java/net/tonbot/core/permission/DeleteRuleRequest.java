package net.tonbot.core.permission;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.tonbot.common.Param;

@ToString
@EqualsAndHashCode
class DeleteRuleRequest {

	@Getter
	private int displayIndex;

	@Param(name = "index", ordinal = 0, description = "The position of the rule to be deleted.")
	public void setDisplayIndex(@Nonnull Integer value) {
		Preconditions.checkArgument(value > 0, "index must be a positive number.");

		this.displayIndex = value;
	}
}
