package com.odder.mixedrecipes.integrations;

import com.evandev.remi.integration.emi.StackManager;
import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.events.StackCacheRebuiltEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

public class StackCacheManager {
    public static final StackCacheManager INSTANCE = new StackCacheManager();

    private final Collection<StackCache> caches = new ArrayList<>();
    private final HashSet<StackCache> busyCaches = new HashSet<>();

    public void notifyChanged() {
        caches.forEach(StackCache::markDirty);
        busyCaches.addAll(caches);

        // TODO: find a better place for dis
        if (MixedRecipes.REMI_ENABLED) {
            StackManager.invalidateStacks();
        }
    }

    public void register(StackCache cache) {
        caches.add(cache);
    }

    @SubscribeEvent
    private void onClientTick(ClientTickEvent.Post ev) {
        for (StackCache cache : new HashSet<>(busyCaches)) {
            if (!cache.isBusy()) {
                busyCaches.remove(cache);
                NeoForge.EVENT_BUS.post(new StackCacheRebuiltEvent(cache));
            }
        }
    }
}
