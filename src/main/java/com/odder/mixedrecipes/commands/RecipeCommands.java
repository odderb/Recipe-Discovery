package com.odder.mixedrecipes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.odder.mixedrecipes.MixedRecipes;
import com.odder.mixedrecipes.attachment.Attachments;
import com.odder.mixedrecipes.recipe.RecipeUtilities;
import com.odder.mixedrecipes.unlocks.UnlockTracker;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiStackList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.HashSet;

public class RecipeCommands {
    @SubscribeEvent
    private static void onRegisterCommands(RegisterCommandsEvent ev) {
        registerCommands(ev.getDispatcher());
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("recipe")
                .then(Commands.literal("wipe").then(Commands.argument("players", EntityArgument.players()).executes(RecipeCommands::wipe)))
        );
    }

    private static int wipe(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "players");

        for (ServerPlayer player : targets) {
            var recipes = RecipeUtilities.getRecipes(player.server.getRecipeManager(), player.recipeBook.known);
            player.resetRecipes(recipes);

            player.setData(Attachments.UNLOCKED_RECIPES, new HashSet<>());
            player.setData(Attachments.SEEN_ITEMS, new HashSet<>());
            player.setData(Attachments.UNVIEWED_ITEMS, new HashSet<>());

            UnlockTracker.INSTANCE.initializeForPlayer(player);
        }

        ctx.getSource().sendSystemMessage(Component.literal("Wiped recipe progress for player(s)"));

        return 0;
    }
}
