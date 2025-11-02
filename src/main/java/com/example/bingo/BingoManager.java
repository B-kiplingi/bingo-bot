package com.example.bingo;

import com.example.data.DataStore;
import com.example.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.data.DataStore.loadState;
import static com.example.data.DataStore.saveState;
import static com.example.util.Util.generateRoundId;

public class BingoManager {
    private boolean active = false;
    private Map<String, BingoRound> rounds = new HashMap<>();
    private String currentRound;

    public void startNewRound(List<String> pool) {
        List<String> selected = Util.pick(pool, 25);
        currentRound = generateRoundId();
        rounds.put(currentRound, new BingoRound(currentRound, selected));
        active = true;
    }

    public void endRound(long winnerId) {
        rounds.get(currentRound).setWinnerId(winnerId);
        active = false;
    }

    public void onPlayerAction() {
        saveState(new DataStore.StateData(rounds, currentRound, active));
    }

    public void load() {
        DataStore.StateData data = loadState();
        if (data != null) {
            rounds = data.rounds;
            currentRound = data.currentRound;
            active = data.active;
        }
    }

    public boolean isActive() {
        return active;
    }

    public BingoRound getCurrentRound() { return rounds.get(currentRound); }
}

