package net.tonbot.core.request;

import lombok.Data;
import lombok.NonNull;
import sx.blah.discord.handle.obj.IGuild;

@Data
public class Context {

	@NonNull
	private IGuild guild;
}
