package com.odder.mixedrecipes.attachment;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Codecs {
    public static final Codec<Set<Holder<Item>>> SEEN =
            ResourceLocation.CODEC.listOf().xmap(
                    list -> list.stream()
                            .map(rl -> BuiltInRegistries.ITEM.getHolder(ResourceKey.create(Registries.ITEM, rl)))
                            .flatMap(Optional::stream)
                            .collect(Collectors.toCollection(HashSet::new)),
                    set -> set.stream()
                            .map(h -> h.unwrapKey().orElseThrow().location())
                            .toList()
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, Set<Holder<Item>>> SEEN_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM).apply(ByteBufCodecs.collection(HashSet::new));
}
