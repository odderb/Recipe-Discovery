package com.odder.mixedrecipes.unlocks.requirements;

import com.odder.mixedrecipes.attachment.Attachments;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class OneOfRequirement implements RecipeUnlockRequirement {
    private final List<Item> contributors = new ArrayList<>();

    public OneOfRequirement() {}

    public static OneOfRequirement from(Collection<ItemStack> items) {
        var req = new OneOfRequirement();
        items.forEach(stack -> req.addContributor(stack.getItem()));
        return req;
    }

    public void addContributor(Item contributor) {
        contributors.add(contributor);
    }

    @Override
    public boolean check(ServerPlayer player) {
        if (contributors.isEmpty()) return true;
        var seen = player.getData(Attachments.SEEN_ITEMS).stream().map(Holder::value).collect(Collectors.toSet());
        return contributors.stream().anyMatch(seen::contains);
    }
}
