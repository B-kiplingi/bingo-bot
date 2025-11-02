package com.example.bingo;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public class BingoSource {
    public static List<String> loadItemsFromChannel(TextChannel channel) {
        return channel.getIterableHistory().complete().stream()
                .map(Message::getContentDisplay)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
