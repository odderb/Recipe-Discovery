package com.odder.mixedrecipes.attachment;

import com.odder.mixedrecipes.integrations.StackCacheManager;
import com.odder.mixedrecipes.packet.NotifyUnlocksPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.HashSet;

public class UnviewedItems {
    public static boolean isUnviewed(Player player, Item item) {
        return player.getData(Attachments.UNVIEWED_ITEMS).contains(BuiltInRegistries.ITEM.getKey(item));
    }

    public static void addUnviewedItems(ServerPlayer player, Collection<ResourceLocation> locations) {
        var copy = getUnviewedItems(player);
        copy.addAll(locations);
        copy.remove(Items.AIR.getDefaultInstance().getItemHolder().getKey().location());
        player.setData(Attachments.UNVIEWED_ITEMS, copy);
        PacketDistributor.sendToPlayer(player, new NotifyUnlocksPacket());
    }

    public static void clearUnviewedItems(ServerPlayer player) {
        player.setData(Attachments.UNVIEWED_ITEMS, new HashSet<>());
        PacketDistributor.sendToPlayer(player, new NotifyUnlocksPacket());
    }

    /**
     * Returns a copy of the player's unviewed items
     * @param player The player whose unviewed items you want to view
     * @return A copy of the unviewed item attachment
     */
    public static HashSet<ResourceLocation> getUnviewedItems(Player player) {
        var data = new HashSet<>(player.getData(Attachments.UNVIEWED_ITEMS));
        data.remove(Items.AIR.getDefaultInstance().getItemHolder().getKey().location());
        return data;
    }
}
