package Clay.Sam.enderdragonRespawn;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class DragonAbilities {

    private static Plugin plugin;
    private final List<Location> beaconLocations = new ArrayList<>();
    private DragonDamageTrack dragonDamageTrack;

    static int dragonPhase; // 0 = normal, 1 = respawn, 2 = minions,


    public DragonAbilities(Plugin plugin, DragonDamageTrack dragonDamageTrack) {
        this.plugin = plugin;
        this.dragonDamageTrack = dragonDamageTrack;
        addBeaconLocations();
        dragonPhase = 0;
        //start schedular for custom abilities
    }


    public void respawnHealBeaconsAbility() {
        for (Location loc : beaconLocations) {
            loc.getWorld().getBlockAt(loc).setType(org.bukkit.Material.BEACON);
            loc.getWorld().getBlockAt(loc.subtract(0, 1, 0)).setType(Material.BEDROCK);
            Bukkit.getLogger().info("Placed beacon at " + loc.toString());
        }
    }

    public void increaseDragonPhase() {
        if(!(dragonPhase >= 2)) {
            dragonPhase++;
        }
    }

    public static int getDragonPhase() {
        return dragonPhase;
    }

    public void spawnMinionsAbility() {

        int minionCount = 3; // Number of minions to spawn per player
        int topNPlayers = 5; // Number of top players to spawn at

        List<Map.Entry<String, Float>> topPlayers = dragonDamageTrack.getTopPlayers(topNPlayers);
        for(int i = 0; i < topPlayers.size(); i++) {
            Map.Entry<String, Float> entry = topPlayers.get(i);
            String playerName = entry.getKey();

            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                Location spawnLocation = player.getLocation().add(0, 5, 0);
                for(int j = 0; j < minionCount; j++) {
                    EnderDragon minion = (EnderDragon) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ENDERMITE);
                    minion.setCustomName("Minion of " + playerName);
                }

                Bukkit.getLogger().info("Spawned minion for " + playerName + " at " + spawnLocation.toString());
            }
        }
    }

    public void angryEnderman() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location playerLocation = player.getLocation();
            for (Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, 10, 10, 10)) {
                if (entity.getType() == EntityType.ENDERMAN) {
                    Enderman enderman = (Enderman) entity;
                    enderman.setTarget(player);
                    Bukkit.getLogger().info("Angered enderman at " + entity.getLocation().toString() + " towards " + player.getName());
                }
            }
        }
    }


    //Helper methods

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

    public static boolean isEventDragon(EnderDragon dragon) {
        NamespacedKey key = new NamespacedKey(plugin, "eventDragon");
        return dragon.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN);
    }


}
