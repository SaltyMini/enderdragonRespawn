package Clay.Sam.enderdragonRespawn;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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

    //
    // Array of abilities, add new abilities here
    //

    private final Runnable[] abilities = {
            this::spawnMinionsAbility,
            this::respawnHealBeaconsAbility,
            this::angryEnderman,
            this::dragonCharge,
            this::dragonBreath,
            this::antiGravityAbility,
            this::rainFireAbility
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

    int fireballCount = 4; // Number of fireballs to spawn per player
    int fireBallsSent = 0;

    public void rainFireAbility() {

        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != world ) continue;
            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnFireball(player), 60);
            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnFireball(player), 120);
            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnFireball(player), 180);
            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnFireball(player), 240);
            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnFireball(player), 300);
        }

    }

    //this may be inefficient
    private void spawnFireball(Player player) {
        Location spawnLoc = player.getLocation().clone().add(0, 10, 0);
        Vector velocity = new Vector(0, -2, 0); // 2x downward

        Fireball fireball = (Fireball) player.getWorld().spawnEntity(spawnLoc, EntityType.FIREBALL);
        fireball.setVelocity(velocity);
        fireball.setIsIncendiary(false);
        fireball.setYield(0);
        fireball.setInvisible(true);
        fireball.setShooter(player);

        // Visible dragon breath
        DragonFireball dragonFireball = (DragonFireball) player.getWorld().spawnEntity(spawnLoc, EntityType.DRAGON_FIREBALL);
        dragonFireball.setVelocity(velocity);
        dragonFireball.setShooter(player);
    }





    public void spawnMinionsAbility() {

        dragonCharge();

        int minionCount = 10; // Number of minions to spawn per player
        int healthModifier = 2; // Health multiplier for minions

        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != world ) continue;
            for(int i = 0; i < minionCount; i++) {
                Endermite minion = (Endermite) world.spawnEntity(player.getLocation(), EntityType.ENDERMITE);
                minion.customName(Component.text("§c§lEvent Minion"));
                //I hate this line but its fine for now
                Objects.requireNonNull(minion.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(Objects.requireNonNull(minion.getAttribute(Attribute.MAX_HEALTH)).getBaseValue() * healthModifier);
            }
        }
    }

    public void antiGravityAbility() {

        PotionEffect potion = new PotionEffect(PotionEffectType.LEVITATION, 100, 1);

        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != world ) continue;
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1.0f, 1.0f);

            player.addPotionEffect(potion);
        }

    }

    public void respawnHealBeaconsAbility() {

        dragonCharge();

        if(beaconLocations.isEmpty()) {
            addBeaconLocations();
        }

        for (Location loc : beaconLocations) {
            loc.getWorld().getBlockAt(loc).setType(org.bukkit.Material.BEACON);
            loc.getWorld().getBlockAt(loc.clone().subtract(0, 1, 0)).setType(Material.BEDROCK);
            plugin.getLogger().info("Placed beacon at " + loc);
        }
    }

    public void angryEnderman() {

        dragonCharge();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() != Bukkit.getWorld("world_the_end") ) continue;
            Location playerLocation = player.getLocation();
            for (Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, 15, 100, 15)) {
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
            DragonMob.dragonRawAll();
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
        if (dragon == null) {
            plugin.getLogger().warning("getTarget: Dragon is null");
            return null;
        }

        List<Player> endPlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String worldName = player.getWorld().getName();
            plugin.getLogger().info("Player " + player.getName() + " is in world: " + worldName);
        
            if (worldName.equalsIgnoreCase("world_the_end")) {
                endPlayers.add(player);
                plugin.getLogger().info("Added player " + player.getName() + " to end players list");
            }
        }

        plugin.getLogger().info("Found " + endPlayers.size() + " players in the End");

        Random rand = new Random();
        if (endPlayers.isEmpty()) {
            plugin.getLogger().warning("No players found in the End world.");
            return null;
        }

        Player selectedTarget = endPlayers.get(rand.nextInt(endPlayers.size()));
        plugin.getLogger().info("Selected target: " + selectedTarget.getName());
        return selectedTarget;
    }


    private void addBeaconLocations() {
        World endWorld = Bukkit.getWorld("world_the_end");
        if (endWorld == null) {
            plugin.getLogger().warning("World 'world_the_end' not found when adding beacon locations.");
            return; // World not found
        }
        beaconLocations.add(new Location(endWorld, -42, 92, -1));
        beaconLocations.add(new Location(endWorld, -34, 83, -25));
        beaconLocations.add(new Location(endWorld, -13, 104, -40));
        beaconLocations.add(new Location(endWorld, 12, 77, -40));
        beaconLocations.add(new Location(endWorld, 33, 98, -25));
        beaconLocations.add(new Location(endWorld, 42, 101, 0));
        beaconLocations.add(new Location(endWorld, 33, 95, 24));
        beaconLocations.add(new Location(endWorld, 12, 90, 40));
        beaconLocations.add(new Location(endWorld, -13, 86, 39));
        beaconLocations.add(new Location(endWorld, -34, 89, 24));
    }
}