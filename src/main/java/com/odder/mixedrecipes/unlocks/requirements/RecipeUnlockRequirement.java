package com.odder.mixedrecipes.unlocks.requirements;

import net.minecraft.server.level.ServerPlayer;

public interface RecipeUnlockRequirement {
    boolean check(ServerPlayer player);
}
