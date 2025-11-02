package com.example.bingo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BingoRound {
    private final String id;
    private long winnerId;
    private final List<String> items;
    private final Map<Long, BingoCard> players = new HashMap<>();

    public BingoRound(String id, List<String> items) {
        this.id = id;
        this.items = new ArrayList<>(items);
    }

    public BingoCard addPlayer(long userId) {
        BingoCard card = new BingoCard(userId, items);
        players.put(userId, card);
        return card;
    }

    public String getId() {
        return id;
    }

    public boolean isPlaying(long userId) {
        return players.containsKey(userId);
    }

    public long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(long winnerId) {
        this.winnerId = winnerId;
    }

    public BingoCard getPlayer(long userId) {
        return players.get(userId);
    }
}

