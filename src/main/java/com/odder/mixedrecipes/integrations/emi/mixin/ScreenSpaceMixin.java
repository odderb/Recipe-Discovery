package com.odder.mixedrecipes.integrations.emi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.UnviewedItems;
import com.odder.mixedrecipes.integrations.StackCacheManager;
import com.odder.mixedrecipes.integrations.emi.EmiStackCache;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EmiScreenManager.ScreenSpace.class, priority = 999, remap = false)
public abstract class ScreenSpaceMixin {
    private static final EmiStackCache EMI_STACK_CACHE = new EmiStackCache();
    private static final ResourceLocation BADGE_TEX = ResourceLocation.fromNamespaceAndPath(MixedRecipes.MODID, "textures/gui/highlight.png");

    @Inject(method = "getStacks", at = @At("HEAD"), cancellable = true, remap = false)
    private void mixedrecipes$getStacks(CallbackInfoReturnable<List<? extends EmiIngredient>> cir) {
        EmiScreenManager.ScreenSpace self = (EmiScreenManager.ScreenSpace)((Object)this);

        // update what EMI was going to return, stack cache handles the rest.
        EMI_STACK_CACHE.updateLastEmiProvided(self.getType(), cir.getReturnValue());

        var cached = EMI_STACK_CACHE.getStacks(self.getType());

        cached.ifPresent(cir::setReturnValue);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void mixedrecipes$render(EmiDrawContext context, int mouseX, int mouseY, float delta, int startIndex, CallbackInfo ci) {
        EmiScreenManager.ScreenSpace self = (EmiScreenManager.ScreenSpace)((Object)this);
        Minecraft inst = Minecraft.getInstance();

        if(inst.player == null) return;

        int i = startIndex;
        List<? extends EmiIngredient> stacks = self.getStacks();
        // TODO: use the native EMI apis
        GuiGraphics g = context.raw();
        outer: for (int yo = 0; yo < self.th; yo++) {
            for (int xo = 0; xo < self.getWidth(yo); xo++) {
                if (i >= stacks.size()) {
                    break outer;
                }
                int cx = self.getX(xo, yo);
                int cy = self.getY(xo, yo);
                int x = cx;
                int y = cy;
                EmiIngredient stack = stacks.get(i++);
                boolean highlight = stack.getEmiStacks().stream().anyMatch(ingredient ->
                        UnviewedItems.isUnviewed(inst.player, ingredient.getItemStack().getItem()));

                if (highlight) {
                    float ticks = inst.gui.getGuiTicks()*0.2f;
                    float scaling = (float)Math.sin(ticks + x + y) * 0.001f;
                    g.pose().pushPose();
                    g.pose().translate(0, 0, 300);
                    g.pose().scale(1.0f+scaling, 1.0f+scaling, 1.0f);
                    g.blit(BADGE_TEX, x, y, 0, 0, 16, 16, 16, 16);
                    g.pose().popPose();
                }
            }
        }
    }

    static {
        StackCacheManager.INSTANCE.register(EMI_STACK_CACHE);
    }
}
