package com.odder.mixedrecipes.events;

import com.odder.mixedrecipes.integrations.StackCache;
import net.neoforged.bus.api.Event;

public class StackCacheRebuiltEvent extends Event {
    private final StackCache cache;

    public StackCacheRebuiltEvent(StackCache cache) {
        this.cache = cache;
    }

    public StackCache getCache() {
        return cache;
    }
}
