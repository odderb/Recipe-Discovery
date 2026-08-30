package com.odder.mixedrecipes.integrations.emi;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.recipe.index.RecipeIndex;
import com.odder.mixedrecipes.unlocks.requirements.OneOfRequirement;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class EmiIndex extends RecipeIndex {
    private final HashSet<EmiRecipe> noBacking = new HashSet<>();

    @Override
    protected RecipeIndex.IndexStatus tickRebuild(RecipeManager recipeManager) {
        if (!EmiReloadManager.isLoaded()) {
            return IndexStatus.BUILDING;
        }

        noBacking.clear();

        var recipes = EmiApi.getRecipeManager().getRecipes();
        for (var recipe : recipes) {
            RecipeHolder<?> backingRecipe = recipe.getBackingRecipe();

            if (recipe.getId() == null || recipe.getId().getPath().startsWith("/"))
                continue;

            if (backingRecipe == null) {
                noBacking.add(recipe);
                continue;
            }

            for (var input : recipe.getInputs()) {
                var stacks = input.getEmiStacks()
                        .stream()
                        .map(EmiStack::getItemStack)
                        .filter(stack -> !stack.isEmpty())
                        .distinct()
                        .toList();

                var items = stacks.stream().map(ItemStack::getItem).toList();

                if (stacks.isEmpty()) continue;

                OneOfRequirement req = OneOfRequirement.from(stacks);

                addRequirementForRecipe(backingRecipe, req);
                items.forEach(item -> addItemForRecipe(item, backingRecipe));
            }
        }

        MixedRecipes.LOGGER.warn("{}/{} recipes had no backing data", noBacking.size(), recipes.size());

        if (!FMLEnvironment.production) {
            dumpNoBackedRecipes();
        }

        return IndexStatus.READY;
    }

    private void dumpNoBackedRecipes() {
        Path path = FMLPaths.GAMEDIR.get().resolve("emi-index-no-backing-recipes.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (EmiRecipe recipe : noBacking) {
                if (recipe.getId() != null) {
                    writer.write(recipe.getId().toString());
                    writer.newLine();
                }
            }
        } catch (IOException e) {}
    }
}
