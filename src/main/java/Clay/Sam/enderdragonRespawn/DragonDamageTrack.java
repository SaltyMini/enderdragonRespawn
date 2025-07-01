package Clay.Sam.enderdragonRespawn;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DragonDamageTrack {

    private static DragonDamageTrack instance = null;

    private final ConcurrentHashMap<String, Float> playerDamageMap = new ConcurrentHashMap<>();

    public static synchronized DragonDamageTrack getInstance() {
        if (instance == null) {
            instance = new DragonDamageTrack();
        }
        return instance;
    }

    public void playerDamageDragonAdd(String playerName, float damage) {
        playerDamageMap.merge(playerName, damage, Float::sum);
    }

    public void clearPlayerDamageMap() {
        playerDamageMap.clear();
    }

    public List<Map.Entry<String, Float>> getTopPlayers(int topN) {
        return playerDamageMap.entrySet().stream()
                .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .toList();
    }

}
