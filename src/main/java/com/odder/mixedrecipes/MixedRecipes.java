package com.odder.mixedrecipes;

import com.odder.mixedrecipes.attachment.Attachments;
import com.odder.mixedrecipes.commands.RecipeCommands;
import com.odder.mixedrecipes.integrations.IntegrationFlags;
import com.odder.mixedrecipes.integrations.StackCacheManager;
import com.odder.mixedrecipes.integrations.remi.CreativeTab;
import com.odder.mixedrecipes.packet.Packets;
import com.odder.mixedrecipes.unlocks.UnlockTracker;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

@Mod(MixedRecipes.MODID)
public class MixedRecipes {
    public static final String MODID = "mixedrecipes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MixedRecipes(IEventBus modEventBus, ModContainer modContainer) {
        IntegrationFlags.REMI = ModList.get().isLoaded("remi");
        IntegrationFlags.EMI = ModList.get().isLoaded("emi");
        IntegrationFlags.PATCHOULI = ModList.get().isLoaded("patchouli");

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        modEventBus.addListener(Packets::register);
        modEventBus.addListener(this::onConfigReloaded);
        Attachments.ATTACHMENTS.register(modEventBus);

        NeoForge.EVENT_BUS.register(UnlockTracker.INSTANCE);
        NeoForge.EVENT_BUS.register(RecipeCommands.class);
        NeoForge.EVENT_BUS.register(StackCacheManager.INSTANCE);

        if (IntegrationFlags.REMI) {
            // Creative tab only exists for REMI integration
            CreativeTab.register(modEventBus);
        }
    }

    private void onConfigReloaded(final ModConfigEvent.Reloading ev) {
        if (EffectiveSide.get().isClient()) {
            StackCacheManager.INSTANCE.notifyChanged();
        }
    }
}
