package com.odder.mixedrecipes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.StackCache;
import com.odder.mixedrecipes.unlocks.UnlockTracker;
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

import java.util.List;

@Mixin(EmiScreenManager.ScreenSpace.class)
public abstract class ScreenSpaceMixin {
    private static final ResourceLocation BADGE_TEX = ResourceLocation.fromNamespaceAndPath(MixedRecipes.MODID, "textures/gui/highlight.png");

    @ModifyReturnValue(method = "getStacks", at = @At("RETURN"))
    private List<? extends EmiIngredient> mixedrecipes$getStacks(List<? extends EmiIngredient> original) {
        EmiScreenManager.ScreenSpace self = (EmiScreenManager.ScreenSpace)((Object)this);

        // update what EMI was going to return, stack cache handles the rest.
        StackCache.INSTANCE.updateLastEmiProvided(self.getType(), original);

        var cached = StackCache.INSTANCE.getStacks(self.getType());

        if (cached.isPresent()) {
            return cached.get();
        }

        return original;
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
                        UnlockTracker.INSTANCE.shouldHighlightUnlock(inst.player, ingredient.getItemStack().getItem()));

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
}
