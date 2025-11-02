package com.example.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Util {
    public static String generateRoundId() {
        return UUID.randomUUID().toString();
    }

    public static List<String> pick(List<String> pool, int amount) {
        if (pool.size() < amount) {
            throw new IllegalArgumentException("Pool too small");
        }
        List<String> result = new ArrayList<>();

        List<String> shuffledPool = new ArrayList<>(pool);

        Collections.shuffle(shuffledPool);

        for (int i = 0; i < amount; i++) {
            result.add(shuffledPool.get(i));
        }
        return result;
    }
}
