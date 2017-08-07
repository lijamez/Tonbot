package com.tonberry.tonbot.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * name: A name for the plugin. (e.g. Decision Maker)
 * summary: A very short summary of what this plugin does. (e.g. Make Important Decisions, Show System Statistics, Tell Time)
 * usageDescription: A description of how this plugin can be used by the user.
 * hidden: Whether if this plugin shoudl be hidden from the "help" menu.
 */
@Data
@Builder
public class PluginResources {

    private final String name;
    private final String shortSummary;
    private final String usageDescription;
    private final boolean hidden;
    private final Set<Object> eventListeners;
    private final Set<PeriodicTask> periodicTasks;

    private PluginResources(
            String name,
            String shortSummary,
            String usageDescription,
            boolean hidden,
            Set<Object> eventListeners,
            Set<PeriodicTask> periodicTasks) {
        this.name = Preconditions.checkNotNull(name, "name must be non-null");
        this.shortSummary = Preconditions.checkNotNull(shortSummary, "shortSummary must be non-null");
        this.usageDescription = Preconditions.checkNotNull(usageDescription, "usageDescription must be non-null.");
        this.hidden = hidden;
        this.eventListeners = eventListeners == null ? ImmutableSet.of() : ImmutableSet.copyOf(eventListeners);
        this.periodicTasks = periodicTasks == null ? ImmutableSet.of() : ImmutableSet.copyOf(periodicTasks);
    }
}
