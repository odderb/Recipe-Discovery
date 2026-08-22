package com.odder.mixedrecipes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.odder.mixedrecipes.MixedRecipesClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentClearHighlightMixin {
    @Inject(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/recipebook/GhostRecipe;clear()V"
            )
    )
    private void mixedrecipes$onRecipeClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir, @Local RecipeHolder<?> lastClickedRecipe) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack output = lastClickedRecipe.value().getResultItem(player.registryAccess());
            MixedRecipesClient.unlocks.markViewed(output.getItemHolder().getKey().location());
        }
    }
}

