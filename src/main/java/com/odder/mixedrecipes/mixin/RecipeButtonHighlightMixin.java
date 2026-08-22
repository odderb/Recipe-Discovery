package com.odder.mixedrecipes.mixin;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.Attachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeButton.class)
public abstract class RecipeButtonHighlightMixin {
    private static final ResourceLocation BADGE_TEX = ResourceLocation.fromNamespaceAndPath(MixedRecipes.MODID, "textures/gui/highlight.png");

    @Shadow
    protected abstract List<RecipeHolder<?>> getOrderedRecipes();

    @Shadow
    private int currentIndex;

    @Shadow
    private RecipeCollection collection;

    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void mixedrecipes$renderRecipeHighlight(GuiGraphics g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        RecipeButton self =  (RecipeButton)(Object)this;
        var inst = Minecraft.getInstance();
        var player = inst.player;
        var unviewed = player.getData(Attachments.UNVIEWED_ITEMS);
        ItemStack itemstack = ((RecipeHolder)getOrderedRecipes().get(this.currentIndex)).value().getResultItem(this.collection.registryAccess());
        if (unviewed.contains(itemstack.getItemHolder().getKey().location())) {
            int x = self.getX();
            int y = self.getY();
            float ticks = inst.gui.getGuiTicks()*0.2f;
            float scaling = (float)Math.sin(ticks + x + y) * 0.001f;
            g.pose().pushPose();
            g.pose().translate(4, 4, 300);
            g.pose().scale(1.0f+scaling, 1.0f+scaling, 1.0f);
            g.blit(BADGE_TEX, x, y, 0, 0, 16, 16, 16, 16);
            g.pose().popPose();
        }
    }
}
