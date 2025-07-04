package Clay.Sam.enderdragonRespawn;

import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DragonDamageTrack {

    private static DragonDamageTrack instance = null;

    private final ConcurrentHashMap<String, Float> playerDamageMap = new ConcurrentHashMap<>();
    private static Plugin plugin;

    public static synchronized DragonDamageTrack getInstance() {
        if (instance == null) {
            instance = new DragonDamageTrack();
        }
        plugin = EnderdragonRespawn.getPlugin();
        return instance;
    }

    public void playerDamageDragonAdd(String playerName, float damage) {
        playerDamageMap.merge(playerName, damage, Float::sum);
        plugin.getLogger().info("Updated damage recorded: " + playerName + " - " + damage + " (Total: " + playerDamageMap.get(playerName) + ")");
    }

    public void clearPlayerDamageMap() {
        playerDamageMap.clear();
        plugin.getLogger().info("Cleared player damage map.");
    }

    public List<Map.Entry<String, Float>> getTopPlayers(int topN) {

        List<Map.Entry<String, Float>> map = playerDamageMap.entrySet().stream()
                .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .toList();

        for(Map.Entry<String, Float> entry : map) {
            plugin.getLogger().info("Size of map: " + playerDamageMap.size());
            if(entry != null) {
                plugin.getLogger().info("Top Player: " + entry.getKey() + " - Damage: " + entry.getValue());
            }
        }

        return map;
    }

}
