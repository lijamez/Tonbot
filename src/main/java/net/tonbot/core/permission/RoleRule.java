package net.tonbot.core.permission;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
class RoleRule extends Rule {

	private final Predicate<List<String>> appliesToRouteMatcher;
	private final IRole role;

	public RoleRule(Predicate<List<String>> appliesToRouteMatcher, IGuild guild, IRole role, boolean allow) {
		super(guild, allow);

		this.appliesToRouteMatcher = Preconditions.checkNotNull(appliesToRouteMatcher,
				"appliesToRouteMatcher must be non-null.");
		this.role = Preconditions.checkNotNull(role, "role must be non-null.");
	}

	@Override
	public boolean appliesTo(List<String> route, IUser user) {
		Preconditions.checkNotNull(route, "route must be non-null.");
		Preconditions.checkNotNull(user, "user must be non-null.");

		if (!appliesToRouteMatcher.test(route)) {
			return false;
		}

		List<IRole> roles = user.getRolesForGuild(this.getGuild());

		return roles.stream()
				.map(role -> role.getLongID())
				.filter(userRoleId -> userRoleId == role.getLongID())
				.findAny()
				.isPresent();
	}
}
