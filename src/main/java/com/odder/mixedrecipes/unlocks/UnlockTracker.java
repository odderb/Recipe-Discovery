package com.odder.mixedrecipes.unlocks;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.Attachments;
import com.odder.mixedrecipes.packet.NotifyUnlocksPacket;
import com.odder.mixedrecipes.recipe.RecipeIndex;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class UnlockTracker {
    public static final UnlockTracker INSTANCE = new UnlockTracker();

    private final RecipeIndex index = new RecipeIndex();

    public UnlockTracker() { }

    public void registerUnlock(ServerPlayer player, Collection<RecipeHolder<?>> recipes) {
        var current = new HashSet<>(player.getData(Attachments.UNVIEWED_ITEMS));
        var unlocks = new HashSet<>(player.getData(Attachments.UNLOCKED_RECIPES));
        var provider = player.level().registryAccess();

        recipes.forEach(holder -> {
                unlocks.add(holder.id());

                ItemStack stack = holder.value().getResultItem(provider);

                if (stack.isEmpty()) return;

                stack.getItemHolder().unwrapKey().ifPresent(key -> {
                    current.add(key.location());
                });
            });

        player.awardRecipes(recipes);

        if (!current.isEmpty()) {
            player.setData(Attachments.UNVIEWED_ITEMS, current);
            player.setData(Attachments.UNLOCKED_RECIPES, unlocks);

            PacketDistributor.sendToPlayer(player, new NotifyUnlocksPacket());

            MixedRecipes.LOGGER.debug("Player {} unlocked {} recipes", player.getStringUUID(), recipes.size());
        }
    }

    public boolean shouldHighlightUnlock(Player player, Item item) {
        return player.getData(Attachments.UNVIEWED_ITEMS).contains(BuiltInRegistries.ITEM.getKey(item));
    }

    public void initializeForPlayer(ServerPlayer player) {
        var unlocks = player.getData(Attachments.UNLOCKED_RECIPES);
        if (unlocks.isEmpty()) {
            registerUnlock(player, index.getDefaultUnlocks());
            player.setData(Attachments.UNVIEWED_ITEMS, new HashSet<>());
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
        for (ServerPlayer player : ev.getServer().getPlayerList().getPlayers()) {
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
            List<RecipeHolder<?>> unlocked = new ArrayList<>();
            for (Holder<Item> holder : newlySeen) {
                var recipes = index.getRecipes(holder.value());
                for (RecipeHolder<?> recipeHolder : recipes) {
                    var requirements = index.getRequirements(recipeHolder);
                    boolean satisfied = requirements.stream().allMatch(req -> req.check(player));
                    if (satisfied) {
                        unlocked.add(recipeHolder);
                    }
                }
            }

            if (unlocked.isEmpty()) return;

            registerUnlock(player, unlocked);
            player.sendSystemMessage(Component.literal(String.format("You unlocked %d recipes", unlocked.size())));
        }
    }
}
