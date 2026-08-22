package com.odder.mixedrecipes.integrations;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.Attachments;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;
import java.util.stream.Collectors;

public class StackCacheManager {
    public static final StackCacheManager INSTANCE = new StackCacheManager();

    private final Collection<StackCache> caches = new ArrayList<>();

    public void notifyChanged() {
        caches.forEach(StackCache::markDirty);
    }

    public void register(StackCache cache) {
        caches.add(cache);
    }
}
