package com.odder.mixedrecipes.integrations.emi;

import com.evandev.remi.feature.stackgroup.EmiGroupStack;
import com.evandev.remi.feature.stackgroup.data.EmiStackGroup;
import com.odder.mixedrecipes.Config;
import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.Attachments;
import com.odder.mixedrecipes.integrations.OutputItemsStackCache;
import com.odder.mixedrecipes.integrations.StackCache;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;
import java.util.stream.Collectors;

public class EmiStackCache implements StackCache {
    private final HashMap<SidebarType, List<? extends EmiIngredient>> cache = new HashMap<>();
    private final HashMap<SidebarType, Boolean> dirty = new HashMap<>();
    private final HashMap<SidebarType, List<? extends EmiIngredient>> lastProvided = new HashMap<>();
    private final OutputItemsStackCache outputItemsStackCache = new OutputItemsStackCache();

    @Override
    public boolean isBusy() {
        return !dirty.isEmpty();
    }

    public void updateLastEmiProvided(SidebarType type, List<? extends EmiIngredient> original) {
        var current = lastProvided.getOrDefault(type, null);

        if (current != original) {
            lastProvided.put(type, original);
            markDirty(type);
        }
    }

    public void markDirty(SidebarType sidebarType) {
        if (dirty.getOrDefault(sidebarType, false)) return;

        outputItemsStackCache.markDirty();
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
        outputItemsStackCache.markDirty();
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
        var seen = player.getData(Attachments.SEEN_ITEMS)
                .stream().map(Holder::value)
                .collect(Collectors.toSet());

        var data = player
                .getData(Attachments.UNVIEWED_ITEMS)
                .stream()
                .map(BuiltInRegistries.ITEM::get)
                .map(ItemStack::new)
                .map(Ingredient::of)
                .map(EmiIngredient::of)
                .collect(Collectors.toSet());

        boolean hideLocked = Config.HIDE_LOCKED_RECIPES.get();
        HashSet<EmiIngredient> groupIngredients = new HashSet<>();

        var sorted = original.stream()
                .filter(ingredient -> {
                    if (hideLocked) {
                        List<EmiStack> stacks = ingredient.getEmiStacks();

                        if (MixedRecipes.REMI_ENABLED) {
                            if (ingredient instanceof EmiGroupStack group) {
                                stacks = group.getItems().stream().flatMap(item -> item.getEmiStacks().stream()).toList();
                            }
                        }
                        boolean hasSeen = stacks.stream().anyMatch(stack -> seen.contains(stack.getItemStack().getItem()));
                        if (hasSeen) {
                            return true;
                        }

                        return ingredient.getEmiStacks().stream().allMatch(stack -> outputItemsStackCache.isUnlocked(stack.getId()));
                    }

                    return true;
                })
                .peek(ingredient -> {
                    if (MixedRecipes.REMI_ENABLED) {
                        if (ingredient instanceof EmiGroupStack group) {
                            groupIngredients.addAll(group.getItems().stream().flatMap(item -> item.getEmiStacks().stream()).toList());
                        }
                    }
                })
                .sorted(Comparator.comparingInt(i -> data.contains(i) && !groupIngredients.contains(i) ? 0 : 1))
                .toList();

        cache.put(sidebarType, sorted);

        MixedRecipes.LOGGER.debug("Recent unlocks rebuilt for sidebar {}", sidebarType);
    }
}
