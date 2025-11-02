package com.example;

import com.example.bot.BotListener;
import com.example.bot.CommandRegistrar;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    public static void main(String[] args) throws IllegalStateException {
        String token = System.getenv("DISCORD_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("DISCORD_TOKEN environment variable not set!");
        }

        JDABuilder builder = JDABuilder.createDefault(token);

        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);

        // Add your main event listener
        builder.addEventListeners(new BotListener());

        // Add command registrar for slash commands
        builder.addEventListeners(new CommandRegistrar());

        builder.build();
        System.out.println("Bingo bot started!");
    }
}
