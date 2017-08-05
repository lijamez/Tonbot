package com.tonberry.tonbot;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Set;

@Data
@Builder
public class Plugin {

    private final String name;
    private final Set<Object> eventListeners;
    private final Set<PeriodicTask> periodicTasks;

    private Plugin(String name, Set<Object> eventListeners, Set<PeriodicTask> periodicTasks) {
        this.name = Preconditions.checkNotNull(name, "name must be non-null");
        this.eventListeners = eventListeners == null ? ImmutableSet.of() : ImmutableSet.copyOf(eventListeners);
        this.periodicTasks = periodicTasks == null ? ImmutableSet.of() : ImmutableSet.copyOf(periodicTasks);
    }
}
