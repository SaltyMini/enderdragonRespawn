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

    World world;


    static DragonAbilities instance = null;

    public DragonAbilities() {
        plugin = EnderdragonRespawn.getPlugin();
        dragonDamageTrack = DragonDamageTrack.getInstance();
        addBeaconLocations();
        dragonPhase = 1;

        world = EnderdragonRespawn.getWorld();
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
    private final Runnable[] abilities = {
            this::spawnMinionsAbility,
            this::respawnHealBeaconsAbility,
            this::angryEnderman,
            this::dragonCharge,
            this::dragonBreath
    };

    public Runnable[] getAbilities() {
        return abilities; // Direct array access
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

        EnderDragon dragon = DragonMob.getEventDragon();
        if(dragon != null) {
            dragon.setPhase(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET);
        }

        int minionCount = 3; // Number of minions to spawn per player
        int topNPlayers = 5; // Number of top players to spawn at

        List<Map.Entry<String, Float>> topPlayers = dragonDamageTrack.getTopPlayers(topNPlayers);
        for (Map.Entry<String, Float> entry : topPlayers) {
            String playerName = entry.getKey();


            Player player = Bukkit.getPlayer(playerName);
            if(player == null) {continue;}
            if(player.getWorld() != world ) continue;

            Location spawnLocation = player.getLocation().clone().add(0, 5, 0);
            for (int j = 0; j < minionCount; j++) {
                Endermite minion = (Endermite) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ENDERMITE);
                minion.customName(Component.text("Minion of " + playerName));
            }
            plugin.getLogger().info("Spawned minion for " + playerName + " at " + spawnLocation);

        }
    }

    public void respawnHealBeaconsAbility() {

        EnderDragon dragon = DragonMob.getEventDragon();
        if(dragon != null) {
            dragon.setPhase(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET);
        }

        for (Location loc : beaconLocations) {
            loc.getWorld().getBlockAt(loc).setType(org.bukkit.Material.BEACON);
            loc.getWorld().getBlockAt(loc.clone().subtract(0, 1, 0)).setType(Material.BEDROCK);
            plugin.getLogger().info("Placed beacon at " + loc);
        }
    }

    public void angryEnderman() {

        EnderDragon dragon = DragonMob.getEventDragon();
        if(dragon != null) {
            dragon.setPhase(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != Bukkit.getWorld("world_the_end") ) continue;
            Location playerLocation = player.getLocation();
            for (Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, 15, 15, 15)) {
                if (entity.getType() == EntityType.ENDERMAN) {
                    Enderman enderman = (Enderman) entity;
                    enderman.setTarget(player);
                    plugin.getLogger().info("Angered enderman at " + entity.getLocation() + " towards " + player.getName());
                }
            }
        }
    }

    public void dragonCharge() {
        EnderDragon dragon = DragonMob.getEventDragon();
        if(dragon != null) {
            Player target = getTarget();
            dragon.setTarget(target);
            dragon.setPhase(EnderDragon.Phase.CHARGE_PLAYER);
        }
    }

    public void dragonBreath() {
        EnderDragon dragon = DragonMob.getEventDragon();
        if(dragon != null) {
            Player target = getTarget();
            dragon.setTarget(target);
            dragon.setPhase(EnderDragon.Phase.BREATH_ATTACK);
        }
    }

    //Helper methods

    public Player getTarget() {
        EnderDragon dragon = DragonMob.getEventDragon();
        if (dragon == null) return null;

        List<Player> endPlayers  = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals("world_the_end")) {
                endPlayers.add(player);
            }
        }

        Random rand = new Random();
        if (endPlayers.isEmpty()) {
            plugin.getLogger().warning("No players found in the End world.");
            return null; // No players in the End world
        }
        Player target = endPlayers.get(rand.nextInt(endPlayers.size()));

        return target;
    }


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