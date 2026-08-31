package com.odder.mixedrecipes.unlocks.client;

import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.MixedRecipesClient;
import com.odder.mixedrecipes.integrations.IntegrationFlags;
import com.odder.mixedrecipes.integrations.StackCacheManager;
import com.odder.mixedrecipes.attachment.Attachments;
import com.odder.mixedrecipes.integrations.remi.CreativeTab;
import com.odder.mixedrecipes.packet.MarkViewedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;

public class ClientUnlocks {
    public void markViewed(ResourceLocation loc) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        PacketDistributor.sendToServer(new MarkViewedPacket(loc));

        // reflect it locally before server syncs
        var localData = new HashSet<>(player.getData(Attachments.UNVIEWED_ITEMS));
        if (localData.remove(loc)) {
            player.setData(Attachments.UNVIEWED_ITEMS, localData);
        }

        StackCacheManager.INSTANCE.notifyChanged();
    }

    @SubscribeEvent
    private void onGameModeChanged(ClientPlayerChangeGameTypeEvent event) {
        StackCacheManager.INSTANCE.notifyChanged();
        MixedRecipes.LOGGER.debug("Gamemode changed, busted stack cache.");
    }

    @SubscribeEvent
    private void onScreenOpening(ScreenEvent.Opening ev) {
        if (ev.getNewScreen() instanceof InventoryScreen) {
            if (IntegrationFlags.REMI) {
                CreativeTab.refresh();
            }
        }
    }

    @SubscribeEvent
    private void onScreenInitPost(ScreenEvent.Init.Post ev) {
        if (!IntegrationFlags.REMI) return;

        try {
            Class<?> mgr = CreativeModeTabManager.class;
            Field tabsField = mgr.getDeclaredField("creativeModeTabs");
            tabsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<CreativeModeTab> tabs = (List<CreativeModeTab>) tabsField.get(null);
            if (tabs == null || tabs.isEmpty()) return;

            CreativeModeTab unviewedTab = CreativeTab.UNVIEWED_TAB.get();
            int current = tabs.indexOf(unviewedTab);
            if (current == -1) return;

            Field indexField = mgr.getDeclaredField("indexCreativeModeTab");
            indexField.setAccessible(true);
            tabs.remove(current);
            tabs.add(Math.min(1, tabs.size()), unviewedTab);
        } catch (Exception e) {
            MixedRecipes.LOGGER.warn("failed to move unviewed tab to second position (exception={})", e.getClass());
        }
    }

//    @SubscribeEvent
//    private void onScreenRenderPost(ScreenEvent.Render.Post ev) {
//        if (!MixedRecipes.REMI_ENABLED) return;
//
//        RemiTabOverlay.render(ev.getGuiGraphics());
//    }
}
