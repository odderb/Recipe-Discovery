package com.odder.mixedrecipes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.odder.mixedrecipes.attachment.Attachments;
import com.odder.mixedrecipes.recipe.LockedRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(targets = "dev.emi.emi.registry.EmiRecipes$Manager", remap = false)
public class GateRecipesMixin {
    @ModifyReturnValue(method = "getRecipesByOutput", at = @At("RETURN"))
    private List<EmiRecipe> mixedrecipes$gateRecipes(List<EmiRecipe> original) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return original;

        var unlocks = mc.player.getData(Attachments.UNLOCKED_RECIPES);
        var rm = mc.level.getRecipeManager();

        return original.stream().map(r -> {
            ResourceLocation id = r.getId();
            if (id == null) return r;
            RecipeHolder<?> holder = rm.byKey(id).orElse(null);
            if (holder == null) return r;
            return unlocks.contains(r.getId()) ? r : new LockedRecipe(r, holder);
        }).toList();
    }
}