package com.odder.mixedrecipes.integrations.emi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.odder.mixedrecipes.attachment.Attachments;
import com.odder.mixedrecipes.integrations.emi.LockedRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(targets = "dev.emi.emi.registry.EmiRecipes$Manager", remap = false)
public class GateRecipesMixin {
    @ModifyReturnValue(method = "getRecipesByOutput", at = @At("RETURN"))
    private List<EmiRecipe> mixedrecipes$gateRecipes(List<EmiRecipe> original) {
        return mixedrecipes$filterToKnown(original);
    }

    @ModifyReturnValue(method = "getRecipesByInput", at = @At("RETURN"))
    private List<EmiRecipe> mixedrecipes$getRecipesByInput(List<EmiRecipe> original) {
        return mixedrecipes$filterToKnown(original);
    }

    @ModifyReturnValue(method = "getRecipe", at = @At("RETURN"))
    private EmiRecipe getRecipe(EmiRecipe original) {
        if (original == null) return null;
        return mixedrecipes$filterToKnown(List.of(original)).getFirst();
    }

    @Unique private List<EmiRecipe> mixedrecipes$filterToKnown(List<EmiRecipe> provided) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return provided;

        if (mc.player.isCreative()) {
            return provided;
        }

        var unlocks = mc.player.getData(Attachments.UNLOCKED_RECIPES);
        var rm = mc.level.getRecipeManager();

        return provided.stream().map(r -> {
            ResourceLocation id = r.getId();
            if (id == null) return r;
            RecipeHolder<?> holder = rm.byKey(id).orElse(null);
            if (holder == null) return r;
            return unlocks.contains(r.getId()) ? r : new LockedRecipe(r, holder);
        }).toList();
    }
}