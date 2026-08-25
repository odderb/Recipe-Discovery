package com.odder.mixedrecipes.recipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LockedRecipe implements EmiRecipe {
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final Font FONT = Minecraft.getInstance().font;

    private final EmiRecipe delegate;
    private final RecipeHolder<?> holder;

    public LockedRecipe(EmiRecipe delegate, RecipeHolder<?> recipeHolder) {
        this.delegate = delegate;
        this.holder = recipeHolder;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return delegate.getCategory();
    }

    @Override
    public ResourceLocation getId() {
        return delegate.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return delegate.getInputs();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return delegate.getOutputs();
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return delegate.getCatalysts();
    }

    @Override
    public int getDisplayWidth() {
        String str = Component.translatable("mixedrecipe.locked").getString();
        String itemText = getItemText().getString();

        return Math.max(FONT.width(itemText), Math.max(delegate.getDisplayWidth(), FONT.width(str)));
    }

    @Override
    public int getDisplayHeight() {
        var txt = getItemText();
        int fontHeight = FONT.lineHeight+2;
        int totalHeight = (txt.getSiblings().size()+1)*fontHeight;
        return Math.max(delegate.getDisplayHeight(), totalHeight);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int fontHeight = Minecraft.getInstance().font.lineHeight+4;

        widgets.addText(
                getItemText(),
                getDisplayWidth() / 2, (getDisplayHeight() / 2) - fontHeight,
                TEXT_COLOR,
                true
        ).horizontalAlign(TextWidget.Alignment.CENTER);

        widgets.addText(
                Component.translatable("mixedrecipe.locked"),
                getDisplayWidth() / 2, getDisplayHeight() / 2 - 4,
                TEXT_COLOR,
                true
        ).horizontalAlign(TextWidget.Alignment.CENTER);
    }

    private Component getItemText() {
        MutableComponent text = Component.empty();
        getOutputs().forEach(output -> text.append(output.getItemStack().getDisplayName()));
        return text;
    }
}