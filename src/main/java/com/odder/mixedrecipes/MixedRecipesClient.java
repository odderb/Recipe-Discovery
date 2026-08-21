package com.odder.mixedrecipes;

import com.odder.mixedrecipes.unlocks.client.ClientUnlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = MixedRecipes.MODID, dist = Dist.CLIENT)
public class MixedRecipesClient {
    public static final ClientUnlocks unlocks = new ClientUnlocks();

    public MixedRecipesClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.register(unlocks);
    }
}
