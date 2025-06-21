package Clay.Sam.enderdragonRespawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class DragonMob implements Listener {

    private Plugin plugin;
    NamespacedKey key;

    public DragonMob(Plugin plugin) {
        this.plugin = plugin;
        key = new NamespacedKey(plugin, "eventDragon");
    }

    public EnderDragon spawnDragon(   ) {

        World world = Bukkit.getWorld("world_the_end");

        if (world == null) {
            Bukkit.getLogger().warning("World 'world_the_end' not found.");
            return null; // World not found
        }

        Location spawnLocation = new Location(world, 0, 128, 0);

        EnderDragon dragon = (EnderDragon) world.spawnEntity(spawnLocation, EntityType.ENDER_DRAGON);

        dragon.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        // Optional: Also add scoreboard tag for command compatibility
        dragon.addScoreboardTag("eventDragon");

        return dragon;
    }



}
