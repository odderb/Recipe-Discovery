package com.odder.mixedrecipes.recipe.handlers;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Collection;

public interface RecipeHandler<T extends Recipe<?>> {
    Collection<Ingredient> retrieve(T recipe);
}
