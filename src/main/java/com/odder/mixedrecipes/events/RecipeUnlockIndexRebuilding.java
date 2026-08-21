package com.odder.mixedrecipes.events;

import com.odder.mixedrecipes.recipe.RecipeIndex;
import net.neoforged.bus.api.Event;

public class RecipeUnlockIndexRebuilding extends Event {
    public final RecipeIndex recipeIndex;

    public RecipeUnlockIndexRebuilding(RecipeIndex index) {
        this.recipeIndex = index;
    }
}
