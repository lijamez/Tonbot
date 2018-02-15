package net.tonbot.core;

import lombok.Data;
import lombok.NonNull;
import net.tonbot.common.Activity;
import net.tonbot.common.Route;

/**
 * Object which represents a matched activity. The matchedRoute may be the
 * canonical route or an alias.
 */
@Data
public class ActivityMatch {
	@NonNull
	private final Activity matchedActivity;

	@NonNull
	private final Route matchedRoute;
}