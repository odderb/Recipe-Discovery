package com.odder.mixedrecipes.packet;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class Packets {
    private static final String VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        var reg = event.registrar(VERSION);
        reg.playToServer(
            MarkViewedPacket.TYPE,
            MarkViewedPacket.STREAM_CODEC,
            MarkViewedPacket::handle
        );
        reg.playToClient(
            NotifyUnlocksPacket.TYPE,
            NotifyUnlocksPacket.STREAM_CODEC,
            NotifyUnlocksPacket::handle
        );
    }
}
