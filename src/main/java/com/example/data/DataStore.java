package com.example.data;

import com.example.bingo.BingoRound;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class DataStore {
    private static final String STATE_FILE = "bingo-state.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveState(DataStore.StateData data) {
        try {
            String json = gson.toJson(data);
            Files.writeString(Paths.get(STATE_FILE), json);
            System.out.println("State saved");
        } catch (IOException e) {
            System.err.println("❌ Failed to save state: " + e.getMessage());
        }
    }

    public static DataStore.StateData loadState() {
        try {
            Path path = Paths.get(STATE_FILE);

            if (!Files.exists(path)) {
                System.out.println("No saved state found, starting fresh");
                return null;
            }

            String json = Files.readString(path);
            DataStore.StateData data = gson.fromJson(json, DataStore.StateData.class);
            System.out.println(data.currentRound + "     " +  data.rounds.toString());
            System.out.println("State loaded");
            return data;
        } catch (IOException e) {
            System.err.println("Failed to load state: " + e.getMessage());
        }
        return null;
    }

    // Wrapper class for JSON serialization
    public static class StateData {
        public Map<String, BingoRound> rounds;
        public String currentRound;
        public boolean active;

        public StateData(Map<String, BingoRound> rounds, String currentRound, boolean active) {
            this.rounds = rounds;
            this.currentRound = currentRound;
            this.active = active;
        }
    }
}
