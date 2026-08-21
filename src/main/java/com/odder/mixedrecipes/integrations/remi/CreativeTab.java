package com.odder.mixedrecipes.integrations.remi;

import com.evandev.remi.feature.creativemodetab.CreativeModeTabManager;
import com.evandev.remi.feature.creativemodetab.gui.itemtab.ItemTab;
import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.Attachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.lang.reflect.Field;
import java.util.Objects;

public class CreativeTab {
    private static class UnviewedItemsDisplayGenerator implements CreativeModeTab.DisplayItemsGenerator {
        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
            if (FMLEnvironment.dist != Dist.CLIENT) return;

            var inst = Minecraft.getInstance();
            var player = inst.player;

            if (player == null) return;

            var level = player.level();
            var ra = level.registryAccess().registryOrThrow(Registries.ITEM);

            player.getData(Attachments.UNVIEWED_ITEMS)
                    .stream()
                    .map(ra::get)
                    .filter(Objects::nonNull)
                    .forEach(output::accept);
        }
    }

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MixedRecipes.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> UNVIEWED_TAB = TABS.register("unviewed_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("tab.mixedrecipe.unviewed"))
                    .icon(Items.NETHER_STAR::getDefaultInstance)
                    .displayItems(new UnviewedItemsDisplayGenerator())
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
        CreativeModeTabManager.reload();
    }

    public static void refresh() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;

        var inst = Minecraft.getInstance();
        var player = inst.player;

        if (player == null) return;

        var level = player.level();
        var ra = level.registryAccess();

        var params = new CreativeModeTab.ItemDisplayParameters(player.connection.enabledFeatures(), true, ra);
        CreativeModeTabs.buildAllTabContents(params);
        if (CreativeModeTabManager.getCurrentTab() == UNVIEWED_TAB.get()) {
            refreshRemiTab(UNVIEWED_TAB.get());
        }
    }

    private static void refreshRemiTab(CreativeModeTab tab) {
        try {
            CreativeModeTab current = CreativeModeTabManager.getCurrentTab();

            if (current != tab) return;

            // bust the tab cache
            Field currentTabField = CreativeModeTabManager.class.getDeclaredField("currentTab");
            currentTabField.setAccessible(true);
            currentTabField.set(null, null);
            CreativeModeTabManager.onTabSelected(new ItemTab(tab));
        } catch (Exception e) {
            MixedRecipes.LOGGER.warn("Exception ({}) refreshing creative tab {}", e.getClass(), tab.getDisplayName().getString());
        }
    }

    @SubscribeEvent
    private static void onScreenOpen(ScreenEvent.Opening ev) {
        if (ev.getNewScreen() instanceof InventoryScreen) {
            refresh();
        }
    }
}
