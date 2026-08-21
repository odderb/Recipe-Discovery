package com.odder.mixedrecipes.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class RecipeUtilities {
    public static ItemStack[] getItemStack(Ingredient ingredient) {
        var items = ingredient.getItems();
        var custom = ingredient.getCustomIngredient();
        if (custom != null) {
            items = custom.toVanilla().getItems();
        }
        return items;
    }
}
