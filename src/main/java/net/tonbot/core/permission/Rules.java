package net.tonbot.core.permission;

import java.util.List;

import com.google.api.client.util.Preconditions;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

public class Rules {

	private Rules() {
	}

	public static Rule anyRouteForRole(IGuild guild, IRole role, boolean allow) {
		Preconditions.checkNotNull(guild, "guild must be non-null.");
		Preconditions.checkNotNull(role, "role must be non-null.");

		return new RoleRule(
				t -> true,
				guild,
				role,
				allow);
	}

	public static Rule routeForRole(List<String> route, IGuild guild, IRole role, boolean allow) {
		Preconditions.checkNotNull(route, "route must be non-null.");
		Preconditions.checkNotNull(guild, "guild must be non-null.");
		Preconditions.checkNotNull(role, "role must be non-null.");

		return new RoleRule(
				t -> route.equals(t),
				guild,
				role,
				allow);
	}
}
