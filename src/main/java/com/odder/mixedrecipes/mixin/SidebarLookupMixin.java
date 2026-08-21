package com.odder.mixedrecipes.mixin;

import com.odder.mixedrecipes.MixedRecipesClient;
import com.odder.mixedrecipes.unlocks.UnlockTracker;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiSidebars;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmiSidebars.class)
public class SidebarLookupMixin {
    @Inject(method = "lookup", at = @At("TAIL"))
    private static void mixedrecipes$onLookup(EmiIngredient stack, CallbackInfo ci) {
        for (EmiStack es : stack.getEmiStacks()) {
            var itemStack = es.getItemStack();
            if (itemStack.isEmpty()) continue;
            itemStack.getItemHolder().unwrapKey().ifPresent(key ->
                    MixedRecipesClient.unlocks.markViewed(key.location()));
        }
    }
}
