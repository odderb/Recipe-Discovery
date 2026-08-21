package com.odder.mixedrecipes.unlocks.requirements;

import com.odder.mixedrecipes.attachment.Attachments;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OneOfRequirement implements RecipeUnlockRequirement {
    private final List<Item> contributors = new ArrayList<>();

    public OneOfRequirement() {}

    public void addContributor(Item contributor) {
        contributors.add(contributor);
    }

    @Override
    public boolean check(ServerPlayer player) {
        var seen = player.getData(Attachments.SEEN_ITEMS).stream().map(Holder::value).collect(Collectors.toSet());
        return contributors.stream().anyMatch(seen::contains);
    }
}
