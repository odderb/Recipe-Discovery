package com.odder.mixedrecipes;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue HIDE_LOCKED_RECIPES = BUILDER.define("hide_locked_recipes", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
