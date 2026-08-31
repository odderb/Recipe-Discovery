package com.odder.mixedrecipes.unlocks;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.Attachments;
import com.odder.mixedrecipes.attachment.UnviewedItems;
import com.odder.mixedrecipes.integrations.IntegrationFlags;
import com.odder.mixedrecipes.integrations.emi.EmiIndex;
import com.odder.mixedrecipes.packet.NotifyUnlocksPacket;
import com.odder.mixedrecipes.recipe.index.RecipeIndex;
import com.odder.mixedrecipes.recipe.index.VanillaIndex;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

public class UnlockTracker {
    public static final UnlockTracker INSTANCE = new UnlockTracker();

    private final RecipeIndex index;

    private final HashMap<ServerPlayer, Collection<ResourceLocation>> owedUnlocks = new HashMap<>();

    public UnlockTracker() {
        if (IntegrationFlags.EMI) {
            index = new EmiIndex();
            MixedRecipes.LOGGER.info("EMI detected, using EMI as an index source.");
        } else {
            index = new VanillaIndex();
            MixedRecipes.LOGGER.info("No recipe viewer detected, falling back to vanilla index source.");
        }
    }

    public void registerUnlock(ServerPlayer player, Collection<ResourceLocation> recipeIds) {
        owedUnlocks.putIfAbsent(player, new ArrayList<>());
        owedUnlocks.get(player).addAll(recipeIds);
    }

    public void initializeForPlayer(ServerPlayer player) {
        var unlocks = player.getData(Attachments.UNLOCKED_RECIPES);
        if (unlocks.isEmpty()) {
            registerUnlock(player, index.getDefaultUnlocks());
            UnviewedItems.clearUnviewedItems(player);
        }
    }

    @SubscribeEvent
    private void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            initializeForPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    private void onServerStarted(ServerStartedEvent ev) {
        index.rebuild(ev.getServer().getRecipeManager());
    }

    @SubscribeEvent
    private void onServerTick(ServerTickEvent.Post ev) {
        index.tick();

        for (ServerPlayer player : ev.getServer().getPlayerList().getPlayers()) {
            handleOwedRecipes(player);
            checkUnlocksForPlayer(player);
        }
    }

    private void checkUnlocksForPlayer(ServerPlayer player) {
        var seen = new HashSet<>(player.getData(Attachments.SEEN_ITEMS));
        var newlySeen = new HashSet<Holder<Item>>();
        var scanned = new ArrayList<ItemStack>();
        var inv = player.getInventory();
        scanned.addAll(inv.armor);
        scanned.addAll(inv.items);
        scanned.addAll(inv.offhand);
        boolean added = false;
        for (ItemStack stack : scanned) {
            if (stack.isEmpty()) continue;

            if (!seen.contains(stack.getItemHolder())) {
                newlySeen.add(stack.getItemHolder());
                added = true;
            }
        }
        if (added) {
            seen.addAll(newlySeen);
            player.setData(Attachments.SEEN_ITEMS, seen);
            List<ResourceLocation> unlocked = new ArrayList<>();
            for (Holder<Item> holder : newlySeen) {
                var locations = index.getRecipeLocations(holder.value());
                for (var location : locations) {
                    var requirements = index.getRequirements(location);
                    boolean satisfied = requirements.stream().allMatch(req -> req.check(player));
                    if (satisfied) {
                        unlocked.add(location);
                    }
                }
            }

            if (unlocked.isEmpty()) return;

            registerUnlock(player, unlocked);
            Component unlockMsg = Component.translatable("mixedrecipes.recipes_unlocked", unlocked.size());
            if (unlocked.size() > 1) {
                unlockMsg = Component.translatable("mixedrecipes.recipes_unlocked_plural", unlocked.size());
            }
            player.sendSystemMessage(unlockMsg);
        }
    }

    private void handleOwedRecipes(ServerPlayer player) {
        if (!owedUnlocks.containsKey(player)) return;

        var recipes = owedUnlocks.get(player);
        var unviewedItems = new HashSet<ResourceLocation>();
        var unlocks = new HashSet<>(player.getData(Attachments.UNLOCKED_RECIPES));

        MinecraftServer server = player.getServer();

        if (server == null) {
            server = ServerLifecycleHooks.getCurrentServer();
        }

        if (server == null) return;

        var provider = server.registryAccess();
        var recipeManager = server.getRecipeManager();

        List<RecipeHolder<?>> vanillaRecipeUnlocks = new ArrayList<>();
        unlocks.addAll(recipes);

        for(ResourceLocation loc : recipes) {
            // Likely only true for vanilla recipes
            recipeManager.byKey(loc).ifPresent(recipe -> {
                vanillaRecipeUnlocks.add(recipe);
                ItemStack stack = recipe.value().getResultItem(provider);
                if (stack.isEmpty()) return;
                stack.getItemHolder().unwrapKey().ifPresent(key -> unviewedItems.add(key.location()));
            });
        }

        player.awardRecipes(vanillaRecipeUnlocks);

        if (!unviewedItems.isEmpty()) {
            UnviewedItems.addUnviewedItems(player, unviewedItems);
        }

        if (!unlocks.isEmpty()) {
            // ensure all default unlocks are given
            unlocks.addAll(index.getDefaultUnlocks());

            player.setData(Attachments.UNLOCKED_RECIPES, unlocks);

            PacketDistributor.sendToPlayer(player, new NotifyUnlocksPacket());

            MixedRecipes.LOGGER.debug("Player {} unlocked {} recipes", player.getStringUUID(), recipes.size());
        }

        owedUnlocks.remove(player);
    }
}
