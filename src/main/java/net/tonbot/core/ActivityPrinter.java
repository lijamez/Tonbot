package net.tonbot.core;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.Prefix;
import net.tonbot.common.Route;

class ActivityPrinter {

	private final String prefix;
	
	@Inject
	public ActivityPrinter(@Prefix String prefix) {
		this.prefix = Preconditions.checkNotNull(prefix, "prefix must be non-null.");
	}
	
	public String getBasicUsage(ActivityDescriptor activityDescriptor) {
		Preconditions.checkNotNull(activityDescriptor, "activityDescriptor must be non-null.");
		
		return getBasicUsage(activityDescriptor.getRoute(), activityDescriptor);
	}
	
	public String getBasicUsage(Route route, ActivityDescriptor activityDescriptor) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("``");
		sb.append(prefix);
		sb.append(StringUtils.join(route, " "));
		sb.append(" ");

		List<String> formattedParams = activityDescriptor.getParameters().stream()
				.map(param -> "<" + param + ">")
				.collect(Collectors.toList());

		sb.append(StringUtils.join(formattedParams, " "));

		sb.append("``");
		sb.append("    ");
		sb.append(activityDescriptor.getDescription());
		
		return sb.toString();
	}
}
