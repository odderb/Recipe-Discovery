package com.odder.mixedrecipes.integrations.emi;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.Attachments;
import com.odder.mixedrecipes.integrations.StackCache;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmiStackCache implements StackCache {
    private final HashMap<SidebarType, List<? extends EmiIngredient>> cache = new HashMap<>();
    private final HashMap<SidebarType, Boolean> dirty = new HashMap<>();
    private final HashMap<SidebarType, List<? extends EmiIngredient>> lastProvided = new HashMap<>();

    public void updateLastEmiProvided(SidebarType type, List<? extends EmiIngredient> original) {
        var current = lastProvided.getOrDefault(type, null);

        if (current != original) {
            lastProvided.put(type, original);
            markDirty(type);
        }
    }

    public void markDirty(SidebarType sidebarType) {
        dirty.put(sidebarType, true);
        var sidebar = EmiScreenManager.getPanelFor(sidebarType);
        if (sidebar != null) {
            sidebar.getSpaces().forEach(space -> space.batcher.repopulate());
        }
        MixedRecipes.LOGGER.debug("EMI sidebar {} was marked dirty", sidebarType);
    }

    public void markDirty() {
        for(SidebarType sidebarType : SidebarType.values()) {
            markDirty(sidebarType);
        }
    }

    public Optional<List<? extends EmiIngredient>> getStacks(SidebarType sidebarType) {
        if (dirty.getOrDefault(sidebarType, false)) {
            dirty.remove(sidebarType);
            createCache(sidebarType);
        }

        if (cache.containsKey(sidebarType)) {
            return Optional.of(cache.get(sidebarType));
        }

        return Optional.empty();
    }

    private void createCache(SidebarType sidebarType) {
        List<? extends EmiIngredient> original = lastProvided.getOrDefault(sidebarType, EmiSidebars.getStacks(sidebarType));

        var player = Minecraft.getInstance().player;

        var data = player
                .getData(Attachments.UNVIEWED_ITEMS)
                .stream()
                .map(BuiltInRegistries.ITEM::get)
                .map(ItemStack::new)
                .map(Ingredient::of)
                .map(EmiIngredient::of)
                .collect(Collectors.toSet());

        var sorted = original.stream()
                .sorted(Comparator.comparingInt(i -> data.contains(i) ? 0 : 1))
                .toList();

        cache.put(sidebarType, sorted);

        MixedRecipes.LOGGER.debug("Recent unlocks rebuilt for sidebar {}", sidebarType);
    }
}
