package com.odder.mixedrecipes.recipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class LockedRecipe extends BasicEmiRecipe {
    private static final int TEXT_COLOR = 0xFFFFFF;

    private final RecipeHolder<?> holder;

    public LockedRecipe(EmiRecipe delegate, RecipeHolder<?> recipeHolder) {
        super(delegate.getCategory(), recipeHolder.id(), delegate.getDisplayWidth(), delegate.getDisplayHeight());
        this.holder = recipeHolder;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(
                Component.translatable("mixedrecipe.locked"),
                getDisplayWidth() / 2, getDisplayHeight() / 2 - 4,
                TEXT_COLOR,
                true
        ).horizontalAlign(TextWidget.Alignment.CENTER);
    }
}