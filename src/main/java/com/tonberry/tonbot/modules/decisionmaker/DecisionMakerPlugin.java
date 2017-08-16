package com.tonberry.tonbot.modules.decisionmaker;

import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.tonberry.tonbot.common.Activity;
import com.tonberry.tonbot.common.TonbotPlugin;
import com.tonberry.tonbot.common.TonbotPluginArgs;

public class DecisionMakerPlugin extends TonbotPlugin {

	private Injector injector;

	public DecisionMakerPlugin(TonbotPluginArgs args) {
		super(args);

		this.injector = Guice.createInjector(new DecisionMakerModule(args.getPrefix()));
	}

	@Override
	public String getFriendlyName() {
		return "Decision Maker";
	}

	@Override
	public String getActionDescription() {
		return "Make Important Decisions";
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
