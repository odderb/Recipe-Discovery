package com.odder.mixedrecipes.integrations.emi;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.integrations.IntegrationFlags;
import com.odder.mixedrecipes.recipe.index.RecipeIndex;
import com.odder.mixedrecipes.unlocks.requirements.OneOfRequirement;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

import java.util.*;

public class EmiIndex extends RecipeIndex {
    private static final Set<ResourceLocation> IGNORED_CATEGORIES = new HashSet<>(List.of(
            ResourceLocation.fromNamespaceAndPath("emi", "anvil_repairing"),
            ResourceLocation.fromNamespaceAndPath("emi", "tag"),
            ResourceLocation.fromNamespaceAndPath("emi", "grinding"),
            ResourceLocation.fromNamespaceAndPath("emi", "world_interaction") // TODO: remove this when we have a way to detect it
    ));

    @Override
    protected RecipeIndex.IndexStatus tickRebuild(RecipeManager recipeManager) {
        if (!EmiReloadManager.isLoaded()) {
            return IndexStatus.BUILDING;
        }

        int unassignedIdCount = 0;
        HashMap<String, Integer> specialTypes = new HashMap<>();

        var recipes = EmiApi.getRecipeManager().getRecipes();
        for (var recipe : recipes) {
            if (recipe.getId() == null) {
                unassignedIdCount +=1;
                continue;
            }

            specialTypes.putIfAbsent(recipe.getCategory().getId().toLanguageKey(), 0);
            specialTypes.put(recipe.getCategory().getId().toLanguageKey(), specialTypes.get(recipe.getCategory().getId().toLanguageKey()) + 1);

            if (IGNORED_CATEGORIES.contains(recipe.getCategory().getId())) {
                addDefaultUnlock(recipe.getId());
            }

            // TODO: remove this and enable special recipe support (sooon)
            if (recipe.getId().getPath().startsWith("/")) continue;

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

                addRequirementForRecipe(recipe.getId(), req);
                items.forEach(item -> addItemForRecipe(item, recipe.getId()));
            }
        }

        if (IntegrationFlags.PATCHOULI) {
            var books = BookRegistry.INSTANCE.books
                    .values()
                    .stream()
                    .toList();

            MixedRecipes.LOGGER.info("There are {} Patchouli books..", books.size());

            for(Book book : books) {
                Optional.ofNullable(EmiApi.getRecipeManager().getRecipe(book.id)).ifPresent(recipe -> {
                    addDefaultUnlock(recipe.getId());
                    MixedRecipes.LOGGER.debug("Patchouli detected, registering book recipe {}", recipe.getId());
                });
            }
        }

        MixedRecipes.LOGGER.warn("{}/{} recipes had no ID", unassignedIdCount, recipes.size());

        for (var type : specialTypes.keySet()) {
            MixedRecipes.LOGGER.debug("Special type {} had count: {}",  type, specialTypes.get(type));
        }

        return IndexStatus.READY;
    }
}
