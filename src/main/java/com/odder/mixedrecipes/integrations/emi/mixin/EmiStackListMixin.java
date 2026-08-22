package com.odder.mixedrecipes.integrations.emi.mixin;

import com.odder.mixedrecipes.integrations.StackCacheManager;
import dev.emi.emi.registry.EmiStackList;
import net.neoforged.fml.util.thread.EffectiveSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmiStackList.class)
public class EmiStackListMixin {
    @Inject(method = "bake", at = @At("TAIL"))
    private static void mixedrecipes$bake(CallbackInfo ci) {
        if (!EffectiveSide.get().isClient()) return;
        StackCacheManager.INSTANCE.notifyChanged();
    }

    @Inject(method = "bakeFiltered", at = @At("TAIL"))
    private static void mixedrecipes$bakeFiltered(CallbackInfo ci) {
        if (!EffectiveSide.get().isClient()) return;
        StackCacheManager.INSTANCE.notifyChanged();
    }

    @Inject(method = "reload", at = @At("TAIL"))
    private static void mixedrecipes$reload(CallbackInfo ci) {
        if (!EffectiveSide.get().isClient()) return;
        StackCacheManager.INSTANCE.notifyChanged();
    }
}
