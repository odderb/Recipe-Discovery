package com.odder.mixedrecipes.recipe.index;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.events.RecipeUnlockIndexRebuilding;
import com.odder.mixedrecipes.recipe.IngredientRetriever;
import com.odder.mixedrecipes.recipe.RecipeUtilities;
import com.odder.mixedrecipes.recipe.handlers.SmithingTransformRecipeHandler;
import com.odder.mixedrecipes.unlocks.requirements.OneOfRequirement;
import com.odder.mixedrecipes.unlocks.requirements.RecipeUnlockRequirement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class RecipeIndex {
    public enum IndexStatus {
        EMPTY,
        BUILDING,
        READY
    }

    protected IndexStatus status = IndexStatus.EMPTY;
    private final HashMap<Item, List<ResourceLocation>> recipeItemIndex = new HashMap<>();
    private final HashMap<ResourceLocation, List<RecipeUnlockRequirement>> recipeToIngredientsIndex = new HashMap<>();
    private final Set<ResourceLocation> defaultUnlocks = new HashSet<>();

    private long indexStartTime;
    private RecipeManager lastProvidedRecipeManager;

    public RecipeIndex() {}

    public List<ResourceLocation> getRecipeLocations(Item item) {
        return recipeItemIndex.getOrDefault(item, Collections.emptyList()).stream().distinct().toList();
    }

    public List<RecipeUnlockRequirement> getRequirements(ResourceLocation recipeId) {
        return recipeToIngredientsIndex.getOrDefault(recipeId, Collections.emptyList());
    }

    public Set<ResourceLocation> getDefaultUnlocks() {
        return defaultUnlocks;
    }

    public void rebuild(RecipeManager recipeManager) {
        recipeItemIndex.clear();
        recipeToIngredientsIndex.clear();
        defaultUnlocks.clear();

        status = IndexStatus.BUILDING;
        NeoForge.EVENT_BUS.post(new RecipeUnlockIndexRebuilding(this));

        indexStartTime = System.nanoTime();
        lastProvidedRecipeManager = recipeManager;
    }

    public void tick() {
        if (status == IndexStatus.READY) return;

        status = tickRebuild(lastProvidedRecipeManager);

        if (status == IndexStatus.READY) {
            double elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - indexStartTime);
            MixedRecipes.LOGGER.info("Recipe index ({}) build took {}ms for {} items with {} default unlocks", getClass(), String.format("%.2f", elapsed), recipeItemIndex.size(), defaultUnlocks.size());
        }
    }

    protected void addItemForRecipe(Item item, ResourceLocation recipeId) {
        recipeItemIndex.computeIfAbsent(item, k -> new ArrayList<>()).add(recipeId);
    }

    protected void addDefaultUnlock(ResourceLocation recipeId) {
        defaultUnlocks.add(recipeId);
    }

    protected void addRequirementForRecipe(ResourceLocation recipeId, RecipeUnlockRequirement recipeUnlockRequirement) {
        recipeToIngredientsIndex.computeIfAbsent(recipeId, k -> new ArrayList<>()).add(recipeUnlockRequirement);
    }

    protected abstract IndexStatus tickRebuild(RecipeManager recipeManager);
}
