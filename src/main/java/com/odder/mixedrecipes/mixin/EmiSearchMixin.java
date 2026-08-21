package com.odder.mixedrecipes.mixin;

import com.odder.mixedrecipes.StackCache;
import dev.emi.emi.search.EmiSearch;
import net.neoforged.fml.util.thread.EffectiveSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmiSearch.class)
public class EmiSearchMixin {
    @Inject(method = "update", at = @At("TAIL"))
    private static void mixedrecipes$update(CallbackInfo ci) {
        if (!EffectiveSide.get().isClient()) return;
        StackCache.INSTANCE.markAllDirty();
    }
}
