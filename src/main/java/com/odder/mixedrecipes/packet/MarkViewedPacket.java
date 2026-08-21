package com.odder.mixedrecipes.packet;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.Attachments;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashSet;

public record MarkViewedPacket(ResourceLocation itemId) implements CustomPacketPayload {
    public static final Type<MarkViewedPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MixedRecipes.MODID, "mark_viewed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarkViewedPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, MarkViewedPacket::itemId,
                    MarkViewedPacket::new);

    public static void handle(MarkViewedPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                var list = new HashSet<>(player.getData(Attachments.UNVIEWED_ITEMS));
                if (list.remove(packet.itemId())) {
                    player.setData(Attachments.UNVIEWED_ITEMS, list);
                }
            }
        });
    }

    @Override public Type<MarkViewedPacket> type() { return TYPE; }
}