package com.odder.mixedrecipes.recipe;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.Attachments;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
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

import java.util.ArrayList;
import java.util.List;

public class LockedRecipe implements EmiRecipe {
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final Font FONT = Minecraft.getInstance().font;
    private static final ResourceLocation MISSING_ITEM_TEXTURE = ResourceLocation.fromNamespaceAndPath(MixedRecipes.MODID, "textures/gui/question-mark.png");

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
        return delegate.getInputs().stream().filter(input -> !input.isEmpty()).toList();
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
        int fontHeight = FONT.lineHeight;
        int totalHeight = (txt.getSiblings().size())*fontHeight;
        totalHeight += fontHeight;
        return totalHeight+18+4;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int displayWidth = getDisplayWidth();
        int heightOffset = 0;

        var itemsTxt = widgets.addText(
                getItemText(),
                displayWidth / 2, heightOffset,
                TEXT_COLOR,
                true
        ).horizontalAlign(TextWidget.Alignment.CENTER);

        heightOffset += itemsTxt.getBounds().height()+2;

        var lockedTxt = widgets.addText(
                Component.translatable("mixedrecipe.locked"),
                displayWidth / 2, heightOffset,
                TEXT_COLOR,
                true
        ).horizontalAlign(TextWidget.Alignment.CENTER);

        heightOffset += lockedTxt.getBounds().height()+2;

        var inputs = getInputs().stream().distinct().toList();
        var seenItems = Minecraft.getInstance().player.getData(Attachments.SEEN_ITEMS);
        int slotOffset = 0;
        for (int i = 0; i < inputs.size(); i++) {
            var input = inputs.get(i);
            boolean shouldRenderItem = input.getEmiStacks().stream().anyMatch(stack -> seenItems.contains(stack.getItemStack().getItemHolder()));
            int slotXOffset = slotOffset + (displayWidth/2) - ((inputs.size()*16)/2);
            SlotWidget slot;
            if (shouldRenderItem) {
                slot = widgets.addSlot(input, slotXOffset, heightOffset);
            } else {
                slot = widgets.addSlot(slotXOffset, heightOffset);
                widgets.addTexture(MISSING_ITEM_TEXTURE, slotXOffset, heightOffset, 16, 16, 0, 0, 16, 16, 16, 16);
            }

            if (slot != null)
                slotOffset += slot.getBounds().width();
        }
    }

    private Component getItemText() {
        MutableComponent text = Component.empty();
        getOutputs().forEach(output -> text.append(output.getItemStack().getDisplayName()));
        return text;
    }
}