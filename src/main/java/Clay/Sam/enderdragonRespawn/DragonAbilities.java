package Clay.Sam.enderdragonRespawn;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class DragonAbilities {

    private static Plugin plugin;
    private final List<Location> beaconLocations = new ArrayList<>();
    private final DragonDamageTrack dragonDamageTrack;

    static DragonAbilities instance = null;

    public DragonAbilities() {
        plugin = EnderdragonRespawn.getPlugin();
        dragonDamageTrack = DragonDamageTrack.getInstance();
        addBeaconLocations();
        dragonPhase = 1;
        //start schedular for custom abilities
    }

    public static synchronized DragonAbilities getInstance() {
        if(instance == null) {
            instance = new DragonAbilities();
        }
        return instance;
    }

    //TODO: USED in DragonEvents to get abilities for the dragon
    //TODO: Add abilities for basic dragon things, make sure to include target getting
    public static List<Runnable> getAbilities() {
        List<Runnable> abilities = new ArrayList<>();
        abilities.add(DragonAbilities.getInstance()::spawnMinionsAbility);
        abilities.add(DragonAbilities.getInstance()::respawnHealBeaconsAbility);
        abilities.add(DragonAbilities.getInstance()::angryEnderman);
        return abilities;
    }


    //
     // Dragon Phase
    //


    int dragonPhase; // 1 - 3
    public void increaseDragonPhase() {
        if(dragonPhase < 3) {
            dragonPhase++;
        }
    }

    public int getDragonPhase() {
        return dragonPhase;
    }

    public void resetDragonPhase() {
        dragonPhase = 1;
    }


    //
     // Abilities
    //


    public void spawnMinionsAbility() {

        int minionCount = 3; // Number of minions to spawn per player
        int topNPlayers = 5; // Number of top players to spawn at

        List<Map.Entry<String, Float>> topPlayers = dragonDamageTrack.getTopPlayers(topNPlayers);
        for (Map.Entry<String, Float> entry : topPlayers) {
            String playerName = entry.getKey();

            Player player = Bukkit.getPlayer(playerName);
            if(player.getWorld() != Bukkit.getWorld("world_the_end") ) continue;
            if (player != null && player.isOnline()) {
                Location spawnLocation = player.getLocation().clone().add(0, 5, 0);
                for (int j = 0; j < minionCount; j++) {
                    Endermite minion = (Endermite) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ENDERMITE);
                    minion.customName(Component.text("Minion of " + playerName));
                }

                plugin.getLogger().info("Spawned minion for " + playerName + " at " + spawnLocation);
            }
        }
    }

    public void respawnHealBeaconsAbility() {
        for (Location loc : beaconLocations) {
            loc.getWorld().getBlockAt(loc).setType(org.bukkit.Material.BEACON);
            loc.getWorld().getBlockAt(loc.clone().subtract(0, 1, 0)).setType(Material.BEDROCK);
            plugin.getLogger().info("Placed beacon at " + loc);
        }
    }

    public void angryEnderman() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != Bukkit.getWorld("world_the_end") ) continue;
            Location playerLocation = player.getLocation();
            for (Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, 10, 10, 10)) {
                if (entity.getType() == EntityType.ENDERMAN) {
                    Enderman enderman = (Enderman) entity;
                    enderman.setTarget(player);
                    plugin.getLogger().info("Angered enderman at " + entity.getLocation() + " towards " + player.getName());
                }
            }
        }
    }


    //Helper methods

    private void addBeaconLocations() {
        World world = Bukkit.getWorld("world_the_end");
        if (world == null) {
            plugin.getLogger().warning("End world not found for beacon locations");
            return;
        }

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




}