package com.odder.mixedrecipes.recipe;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.recipe.handlers.RecipeHandler;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Collection;
import java.util.HashMap;

public class IngredientRetriever {
    private final HashMap<Class<?>, RecipeHandler<?>> handlers = new HashMap<>();

    public <T extends Recipe<?>> void registerHandler(Class<T> clazz, RecipeHandler<T> handler) {
        handlers.putIfAbsent(clazz, handler);
    }

    public Collection<Ingredient> getStacks(Recipe<?> recipe) {
        RecipeHandler<?> handler = handlers.getOrDefault(recipe.getClass(), null);
        RecipeHandler<Recipe<?>> casted = (RecipeHandler<Recipe<?>>) handler;
        if (handler == null) {
            var ingredients = recipe.getIngredients();
            if (ingredients.isEmpty()) {
                MixedRecipes.LOGGER.debug("Recipe handler used default, but had empty ingredients. Type: {}", recipe.getClass());
            }
            return ingredients;
        }
        return casted.retrieve(recipe);
    }
}
