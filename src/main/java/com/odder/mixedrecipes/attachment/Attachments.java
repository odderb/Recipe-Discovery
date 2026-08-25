package com.odder.mixedrecipes.attachment;

import com.odder.mixedrecipes.MixedRecipes;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class Attachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MixedRecipes.MODID);

    public static final Supplier<AttachmentType<Set<ResourceLocation>>> UNVIEWED_ITEMS =
            ATTACHMENTS.register("unviewed_items", () ->
                    AttachmentType.<Set<ResourceLocation>>builder(() -> new HashSet<>())
                            .serialize(ResourceLocation.CODEC.listOf().xmap(HashSet::new, ArrayList::new))
                            .sync(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).map(HashSet::new, ArrayList::new))
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<Set<ResourceLocation>>> UNLOCKED_RECIPES =
            ATTACHMENTS.register("unlocked_recipes", () ->
                    AttachmentType.<Set<ResourceLocation>>builder(() -> new HashSet<>())
                            .serialize(ResourceLocation.CODEC.listOf().xmap(HashSet::new, ArrayList::new))
                            .sync(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).map(HashSet::new, ArrayList::new))
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<Set<Holder<Item>>>> SEEN_ITEMS =
            ATTACHMENTS.register("seen_items", () ->
                    AttachmentType.<Set<Holder<Item>>>builder(() -> new HashSet<>())
                            .serialize(Codecs.SEEN)
                            .sync(Codecs.SEEN_STREAM_CODEC)
                            .copyOnDeath()
                            .build());
}
