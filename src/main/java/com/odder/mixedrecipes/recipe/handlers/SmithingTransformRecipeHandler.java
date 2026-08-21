package com.odder.mixedrecipes.recipe.handlers;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

import java.util.Collection;
import java.util.List;

public class SmithingTransformRecipeHandler implements RecipeHandler<SmithingTransformRecipe> {
    @Override
    public Collection<Ingredient> retrieve(SmithingTransformRecipe recipe) {
        return List.of(recipe.template, recipe.base, recipe.addition);
    }
}
