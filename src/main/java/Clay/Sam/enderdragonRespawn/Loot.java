package Clay.Sam.enderdragonRespawn;

import org.bukkit.Material;
import java.util.Random;

public class Loot {
    
    private static final Random RANDOM = new Random();

    public record LootItem(Material material, int amount, double weight) {
    }

    public static final LootItem[] FIRST_PLACE = {
        new LootItem(Material.NETHERITE_INGOT, 3, 10.0),
        new LootItem(Material.ENCHANTED_GOLDEN_APPLE, 2, 15.0),
        new LootItem(Material.DIAMOND, 8, 25.0),
        new LootItem(Material.EMERALD, 12, 35.0),
        new LootItem(Material.GOLD_INGOT, 16, 15.0)
    };
    
    public static final LootItem[] SECOND_PLACE = {
        new LootItem(Material.DIAMOND, 4, 20.0),
        new LootItem(Material.EMERALD, 8, 30.0),
        new LootItem(Material.GOLD_INGOT, 10, 35.0),
        new LootItem(Material.IRON_INGOT, 16, 15.0)
    };
    
    public static final LootItem[] THIRD_PLACE = {
        new LootItem(Material.EMERALD, 4, 25.0),
        new LootItem(Material.GOLD_INGOT, 6, 30.0),
        new LootItem(Material.IRON_INGOT, 12, 35.0),
        new LootItem(Material.COAL, 32, 10.0)
    };

    public static LootItem pickRandomLoot(LootItem[] lootItems) {
        // Calculate total weight
        double totalWeight = 0;
        for (LootItem item : lootItems) {
            totalWeight += item.weight;
        }
        
        // Pick random value
        double randomValue = RANDOM.nextDouble() * totalWeight;
        
        // Find the item
        double currentWeight = 0;
        for (LootItem item : lootItems) {
            currentWeight += item.weight;
            if (randomValue <= currentWeight) {
                return item;
            }
        }
        
        return lootItems[lootItems.length - 1]; // Fallback
    }
}