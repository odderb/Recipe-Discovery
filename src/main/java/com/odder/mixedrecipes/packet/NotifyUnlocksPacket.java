package com.odder.mixedrecipes.packet;

import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.StackCache;
import com.odder.mixedrecipes.integrations.remi.CreativeTab;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NotifyUnlocksPacket() implements CustomPacketPayload {
    public static Type<NotifyUnlocksPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MixedRecipes.MODID, "notify_unlocks_packet"));

    public static StreamCodec<ByteBuf, NotifyUnlocksPacket> STREAM_CODEC = StreamCodec.unit(new NotifyUnlocksPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NotifyUnlocksPacket packet, IPayloadContext ctx) {
        if (ctx.player() instanceof LocalPlayer) {
            StackCache.INSTANCE.markAllDirty();
            if (MixedRecipes.REMI_ENABLED) {
                CreativeTab.refresh();
            }
        }
    }
}
