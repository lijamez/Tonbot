package com.tonberry.tonbot.modules.systeminfo;

import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.tonberry.tonbot.common.Activity;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

public class SystemInfoPlugin extends TonbotPlugin {

	private Injector injector;

	public SystemInfoPlugin(TonbotPluginArgs args) {
		super(args);

		this.injector = Guice.createInjector(new SystemInfoModule(args.getPrefix()));
	}

	@Override
	public String getFriendlyName() {
		return "System Info";
	}

	@Override
	public String getActionDescription() {
		return "Display System Vitals";
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public Set<Activity> getActivities() {
		return injector.getInstance(Key.get(new TypeLiteral<Set<Activity>>() {
		}));
	}
}
