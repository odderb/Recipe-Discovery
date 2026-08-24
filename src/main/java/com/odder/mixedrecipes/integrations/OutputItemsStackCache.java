package com.odder.mixedrecipes.integrations;

import com.odder.mixedrecipes.attachment.Attachments;
import com.odder.mixedrecipes.recipe.RecipeUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashSet;

public class OutputItemsStackCache implements StackCache {
    private final HashSet<ResourceLocation> locationsWithOutputs = new HashSet<>();

    public boolean isUnlocked(ResourceLocation location) {
        if (locationsWithOutputs.isEmpty()) {
            markDirty();
        }

        return locationsWithOutputs.contains(location);
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public void markDirty() {
        locationsWithOutputs.clear();

        var player = Minecraft.getInstance().player;
        var recipes = RecipeUtilities.getRecipes(
                player.connection.getRecipeManager(),
                player.getData(Attachments.UNLOCKED_RECIPES)
        );
        for (var recipe : recipes) {
            var output = recipe.value().getResultItem(player.registryAccess());
            locationsWithOutputs.add(output.getItemHolder().getKey().location());
        }
    }
}
