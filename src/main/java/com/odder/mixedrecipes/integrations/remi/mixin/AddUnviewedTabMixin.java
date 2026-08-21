package com.odder.mixedrecipes.integrations.remi.mixin;

import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.odder.mixedrecipes.integrations.remi.CreativeTab;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = CreativeModeTabManager.class, remap = false)
public class AddUnviewedTabMixin {
    @Inject(method = "getVisibleCreativeModeTabs", at = @At("TAIL"), cancellable = true)
    private static void mixedrecipes$shouldHideTab(CallbackInfoReturnable<List<CreativeModeTab>> cir) {
        if (!CreativeTab.UNVIEWED_TAB.isBound()) return;

        var current = cir.getReturnValue();

        if (!current.contains(CreativeTab.UNVIEWED_TAB.get())) {
            current.add(CreativeTab.UNVIEWED_TAB.get());
        }
    }
}
