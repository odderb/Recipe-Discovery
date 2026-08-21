package com.odder.mixedrecipes.mixin;

import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(RecipeToast.class)
public class RecipePopupMixin {
    @Shadow
    @Final
    private List<RecipeHolder<?>> recipes;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", ordinal = 1), index = 1)
    private Component mixedrecipes$showCount(Component original) {
        if (recipes.size() > 1) {
            return Component.translatable("mixedrecipes.recipes_unlocked_plural", recipes.size());
        }

        return Component.translatable("mixedrecipes.recipes_unlocked", recipes.size());
    }
}
