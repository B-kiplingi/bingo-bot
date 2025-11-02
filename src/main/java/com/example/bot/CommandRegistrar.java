package com.example.bot;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

public class CommandRegistrar extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        var jda = event.getJDA();

        // Global commands
        jda.upsertCommand("bingo-start", "Start a new bingo round").queue();
        jda.upsertCommand("bingo-join", "Join the current bingo round").queue();
        jda.upsertCommand("bingo-card", "Show your bingo card").queue();
        jda.upsertCommand("bingo-check", "Mark an item on your bingo card")
                .addOptions(new OptionData(OptionType.STRING, "item", "The item text to mark", true))
                .queue();

        System.out.println("Commands registered!");
    }
}