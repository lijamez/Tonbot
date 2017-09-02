package net.tonbot.core.permission;

import java.util.List;

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

	private final List<String> appliesToRoute;
	private final IRole role;

	public RoleRule(List<String> appliesToRoute, IGuild guild, IRole role, boolean allow) {
		super(guild, allow);

		this.appliesToRoute = Preconditions.checkNotNull(appliesToRoute,
				"appliesToRoute must be non-null.");
		this.role = Preconditions.checkNotNull(role, "role must be non-null.");
	}

	@Override
	public boolean appliesTo(List<String> route, IUser user) {
		Preconditions.checkNotNull(route, "route must be non-null.");
		Preconditions.checkNotNull(user, "user must be non-null.");

		if (!appliesToRoute.equals(route)) {
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
