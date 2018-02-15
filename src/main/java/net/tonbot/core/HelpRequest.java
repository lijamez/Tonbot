package net.tonbot.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.tonbot.common.Param;

@ToString
@EqualsAndHashCode
public class HelpRequest {

	@Getter
	@Param(name = "route", ordinal = 0, captureRemaining = true)
	private String route;
}
