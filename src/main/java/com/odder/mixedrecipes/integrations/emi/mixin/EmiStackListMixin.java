package com.odder.mixedrecipes.integrations.emi.mixin;

import com.odder.mixedrecipes.integrations.StackCacheManager;
import dev.emi.emi.registry.EmiStackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmiStackList.class)
public class EmiStackListMixin {
    @Inject(method = "bake", at = @At("HEAD"))
    private static void mixedrecipes$bake(CallbackInfo ci) {
        StackCacheManager.INSTANCE.notifyChanged();
    }

    @Inject(method = "bakeFiltered", at = @At("HEAD"))
    private static void mixedrecipes$bakeFiltered(CallbackInfo ci) {
        StackCacheManager.INSTANCE.notifyChanged();
    }

    @Inject(method = "reload", at = @At("HEAD"))
    private static void mixedrecipes$reload(CallbackInfo ci) {
        StackCacheManager.INSTANCE.notifyChanged();
    }
}
