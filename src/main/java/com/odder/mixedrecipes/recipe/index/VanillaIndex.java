package com.odder.mixedrecipes.recipe.index;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.recipe.IngredientRetriever;
import com.odder.mixedrecipes.recipe.RecipeUtilities;
import com.odder.mixedrecipes.recipe.handlers.SmithingTransformRecipeHandler;
import com.odder.mixedrecipes.unlocks.requirements.OneOfRequirement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.HashSet;

public class VanillaIndex extends RecipeIndex {
    private final IngredientRetriever ingredientRetriever = new IngredientRetriever();

    public VanillaIndex() {
        ingredientRetriever.registerHandler(SmithingTransformRecipe.class, new SmithingTransformRecipeHandler());
    }

    @Override
    protected RecipeIndex.IndexStatus tickRebuild(RecipeManager recipeManager) {
        var recipes = recipeManager.getRecipes();
        for (RecipeHolder<?> holder : recipes) {
            if (holder.value().isSpecial()) continue;

            HashSet<Item> inputs = new HashSet<>();
            for (Ingredient ingredient : ingredientRetriever.getStacks(holder.value())) {
                var req = new OneOfRequirement();
                boolean added = false;

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
                    addRequirementForRecipe(holder, req);
                }
            }
            for (var input : inputs) {
                addItemForRecipe(input, holder);
            }
        }

        for (RecipeHolder<?> holder : recipes) {
            var requirements = getRequirements(holder);
            if (requirements.isEmpty()) {
                addDefaultUnlock(holder);
            }
        }

        return IndexStatus.READY;
    }
}
