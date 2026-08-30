package com.odder.mixedrecipes.recipe.index;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.events.RecipeUnlockIndexRebuilding;
import com.odder.mixedrecipes.recipe.IngredientRetriever;
import com.odder.mixedrecipes.recipe.RecipeUtilities;
import com.odder.mixedrecipes.recipe.handlers.SmithingTransformRecipeHandler;
import com.odder.mixedrecipes.unlocks.requirements.OneOfRequirement;
import com.odder.mixedrecipes.unlocks.requirements.RecipeUnlockRequirement;
import net.minecraft.core.registries.BuiltInRegistries;
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
    private final HashMap<Item, List<RecipeHolder<?>>> recipeItemIndex = new HashMap<>();
    private final HashMap<RecipeHolder<?>, List<RecipeUnlockRequirement>> recipeToIngredientsIndex = new HashMap<>();
    private final Set<RecipeHolder<?>> defaultUnlocks = new HashSet<>();

    private long indexStartTime;
    private RecipeManager lastProvidedRecipeManager;

    public RecipeIndex() {}

    public List<RecipeHolder<?>> getRecipes(Item item) {
        return recipeItemIndex.getOrDefault(item, Collections.emptyList()).stream().distinct().toList();
    }

    public List<RecipeUnlockRequirement> getRequirements(RecipeHolder<?> recipeHolder) {
        return recipeToIngredientsIndex.getOrDefault(recipeHolder, Collections.emptyList());
    }

    public Set<RecipeHolder<?>> getDefaultUnlocks() {
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

    protected void addItemForRecipe(Item item, RecipeHolder<?> holder) {
        recipeItemIndex.computeIfAbsent(item, k -> new ArrayList<>()).add(holder);
    }

    protected void addDefaultUnlock(RecipeHolder<?> recipeHolder) {
        defaultUnlocks.add(recipeHolder);
    }

    protected void addRequirementForRecipe(RecipeHolder<?> recipeHolder, RecipeUnlockRequirement recipeUnlockRequirement) {
        recipeToIngredientsIndex.computeIfAbsent(recipeHolder, k -> new ArrayList<>()).add(recipeUnlockRequirement);
    }

    protected abstract IndexStatus tickRebuild(RecipeManager recipeManager);
}
