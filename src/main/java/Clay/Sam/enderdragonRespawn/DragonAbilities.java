package Clay.Sam.enderdragonRespawn;

import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class DragonAbilities implements Listener {

    private static Plugin plugin;

    private final List<Location> beaconLocations = new ArrayList<>();


    public DragonAbilities(Plugin plugin) {
        this.plugin = plugin;

        addBeaconLocations();
    }


    public void respawnHealBeaconsAbility() {
        for (Location loc : beaconLocations) {
            loc.getWorld().getBlockAt(loc).setType(org.bukkit.Material.BEACON);
            loc.getWorld().getBlockAt(loc.subtract(0, 1, 0)).setType(Material.BEDROCK);
            Bukkit.getLogger().info("Placed beacon at " + loc.toString());
        }
    }

    public void spawnMinionsAbility() {

        //spawn minions on top 3 players
    }



    private void addBeaconLocations() {
        World world = Bukkit.getWorld("world_the_end");
        beaconLocations.add(new Location(world, -42, 92, -1));
        beaconLocations.add(new Location(world, -34, 83, -25));
        beaconLocations.add(new Location(world, -13, 104, -40));
        beaconLocations.add(new Location(world, 12, 77, -40));
        beaconLocations.add(new Location(world, 33, 98, -25));
        beaconLocations.add(new Location(world, 42, 101, 0));
        beaconLocations.add(new Location(world, 33, 95, 24));
        beaconLocations.add(new Location(world, 12, 90, 40));
        beaconLocations.add(new Location(world, -13, 86, 39));
        beaconLocations.add(new Location(world, -34, 89, 24));
    }

    //Helper methods

    public static boolean isEventDragon(EnderDragon dragon) {
        NamespacedKey key = new NamespacedKey(plugin, "eventDragon");
        return dragon.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN);
    }


}
