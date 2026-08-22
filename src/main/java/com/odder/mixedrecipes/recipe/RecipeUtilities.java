package com.odder.mixedrecipes.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.*;

public class RecipeUtilities {
    public static ItemStack[] getItemStack(Ingredient ingredient) {
        var items = ingredient.getItems();
        var custom = ingredient.getCustomIngredient();
        if (custom != null) {
            items = custom.toVanilla().getItems();
        }
        return items;
    }

    public static List<RecipeHolder<?>> getRecipes(RecipeManager manager, Collection<ResourceLocation> locations) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (ResourceLocation loc : locations) {
            manager.byKey(loc).ifPresent(recipes::add);
        }
        return recipes;
    }
}
