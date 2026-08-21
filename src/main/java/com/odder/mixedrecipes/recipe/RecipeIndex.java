package com.odder.mixedrecipes.recipe;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.events.RecipeUnlockIndexRebuilding;
import com.odder.mixedrecipes.recipe.handlers.SmithingTransformRecipeHandler;
import com.odder.mixedrecipes.unlocks.requirements.OneOfRequirement;
import com.odder.mixedrecipes.unlocks.requirements.RecipeUnlockRequirement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RecipeIndex {
    private final HashMap<Item, List<RecipeHolder<?>>> recipeItemIndex = new HashMap<>();
    private final HashMap<RecipeHolder<?>, List<RecipeUnlockRequirement>> recipeToIngredientsIndex = new HashMap<>();
    private final Set<RecipeHolder<?>> defaultUnlocks = new HashSet<>();
    private final IngredientRetriever ingredientRetriever = new IngredientRetriever();

    public RecipeIndex() {
        ingredientRetriever.registerHandler(SmithingTransformRecipe.class, new SmithingTransformRecipeHandler());
    }

    public IngredientRetriever getIngredientRetriever() {
        return ingredientRetriever;
    }

    public List<RecipeHolder<?>> getRecipes(Item item) {
        return recipeItemIndex.getOrDefault(item, new ArrayList<>());
    }

    public List<RecipeUnlockRequirement> getRequirements(RecipeHolder<?> recipeHolder) {
        return recipeToIngredientsIndex.getOrDefault(recipeHolder, new ArrayList<>());
    }

    public Set<RecipeHolder<?>> getDefaultUnlocks() {
        return defaultUnlocks;
    }

    public void rebuild(RecipeManager recipeManager) {
        recipeItemIndex.clear();
        recipeToIngredientsIndex.clear();

        NeoForge.EVENT_BUS.post(new RecipeUnlockIndexRebuilding(this));

        var recipes = recipeManager.getRecipes();
        long start = System.nanoTime();

        for (RecipeHolder<?> holder : recipes) {
            HashSet<Item> inputs = new HashSet<>();
            for (Ingredient ingredient : ingredientRetriever.getStacks(holder.value())) {
                var req = new OneOfRequirement();
                boolean added = false;

                if (holder.value().isSpecial()) continue;

                ItemStack[] stacks = RecipeUtilities.getItemStack(ingredient);

                if (stacks.length == 0) {
                    MixedRecipes.LOGGER.debug("Ingredient returned no item stacks [ingredient={}, recipe={}, id={}]", ingredient.getClass(), holder.value().getClass(), holder.id());
                }

                for (ItemStack stack : stacks) {
                    if (!stack.isEmpty()) {
                        added = true;
                        req.addContributor(stack.getItem());
                        inputs.add(stack.getItem());
                    }
                }
                if (added) {
                    recipeToIngredientsIndex.computeIfAbsent(holder, k -> new ArrayList<>()).add(req);
                }
            }
            for (var input : inputs) {
                recipeItemIndex.computeIfAbsent(input, k -> new ArrayList<>()).add(holder);
            }
        }

        for (RecipeHolder<?> holder : recipes) {
            var requirements = recipeToIngredientsIndex.getOrDefault(holder, Collections.emptyList());
            if (requirements.isEmpty()) {
                defaultUnlocks.add(holder);
            }
        }

        double elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        MixedRecipes.LOGGER.info("Recipe index build took {}ms for {} items with {} default unlocks", String.format("%.2f", elapsed), recipeItemIndex.size(), defaultUnlocks.size());
    }
}
