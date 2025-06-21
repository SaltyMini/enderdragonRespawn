package Clay.Sam.enderdragonRespawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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

    //Guna do some config stuff here
    private final double damageMultiplier = 1.5;
    private final List<Location> beaconLocations = new ArrayList<>();

    private Plugin plugin;
    NamespacedKey key;

    HashMap<UUID, Float> playerDamageMap = new HashMap<>();

    public DragonAbilities(Plugin plugin) {
        this.plugin = plugin;
        key = new NamespacedKey(plugin, "eventDragon");

        addBeaconLocations();
    }

    // when the dragon attacks/damages a player
    @EventHandler
    public void onDragonAttack(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getDamager();
        if (!isEventDragon(dragon)) return;

        // Your custom code when event dragon attacks
        Bukkit.getLogger().info("Event Dragon attacked " + event.getEntity().getName());

        // Example: Increase damage
        event.setDamage(event.getDamage() * damageMultiplier);

        // Example: Special effects
        event.getEntity().getLocation().getWorld().createExplosion(
                event.getEntity().getLocation(), 2.0f, false, false
        );
    }

    //Player damages dragon
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (!isEventDragon(dragon)) return;

        if(!(event.getDamageSource().getCausingEntity() instanceof Player)) return;

        UUID playerUUID = event.getDamageSource().getCausingEntity().getUniqueId();
        float damage = (float) event.getDamage();

        playerDamageMap.merge(playerUUID, damage, Float::sum);


    }

    // Event: When the dragon targets a player
    @EventHandler
    public void onDragonTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (!isEventDragon(dragon)) return;

        Bukkit.getLogger().info("Event Dragon is targeting " +
                (event.getTarget() != null ? event.getTarget().getName() : "no one"));

        if (event.getTarget() != null) {
            Bukkit.broadcastMessage("§c[Event Dragon] §fThe Event Dragon has targeted " +
                    event.getTarget().getName() + "!");
        }
    }

    // Event: When the dragon shoots fireballs
    @EventHandler
    public void onDragonFireball(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getEntity().getShooter();
        if (!isEventDragon(dragon)) return;

        Bukkit.getLogger().info("Event Dragon launched a fireball!");

        // Example: Make fireballs faster
        event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(2.0));

        // Example: Spawn multiple fireballs
        for (int i = 0; i < 2; i++) {
            dragon.getWorld().spawn(dragon.getLocation(),
                    event.getEntity().getClass());
        }
    }


    @EventHandler
    public void onDragonPhaseChange(EnderDragonChangePhaseEvent event) {
        EnderDragon dragon = event.getEntity();
        if (!isEventDragon(dragon)) return;

        EnderDragon.Phase newPhase = event.getNewPhase();
        EnderDragon.Phase currentPhase = event.getCurrentPhase();

        // Check for perching phases
        if (newPhase == EnderDragon.Phase.LAND_ON_PORTAL) {
            // Dragon is about to perch or is perching
            Bukkit.getLogger().info("Event Dragon is perching!");
            Bukkit.broadcastMessage("§canceling dragon perch");
            event.setCancelled(true);
        }

        // Log all phase changes for debugging
        Bukkit.getLogger().info("Event Dragon phase changed from " +
                (currentPhase != null ? currentPhase.name() : "null") +
                " to " + newPhase.name());
    }



    // Event: When the dragon dies
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (!isEventDragon(dragon)) return;

        Bukkit.broadcastMessage("§6[Event Dragon] §fThe mighty Event Dragon has been slain!");

        dragon.getWorld().createExplosion(dragon.getLocation(), 5.0f, false, false);

        Map<UUID, Float> sortedScores = playerDamageMap.entrySet()
                .stream()
                .sorted(Map.Entry.<UUID, Float>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        //get dmg winner
        if (!sortedScores.isEmpty()) {

            Iterator<UUID> iterator = sortedScores.keySet().iterator();

            Player topPlayer = iterator.hasNext() ? Bukkit.getPlayer(iterator.next()) : null;
            Player secondPlayer = iterator.hasNext() ? Bukkit.getPlayer(iterator.next()) : null;
            Player thirdPlayer = iterator.hasNext() ? Bukkit.getPlayer(iterator.next()) : null;

            if (topPlayer != null) {
                //TODO: give top player a reward
            } else if (secondPlayer != null) {
                //TODO: give second player a reward
            } else if (thirdPlayer != null) {
                //TODO: give third player a reward
            }
            Bukkit.broadcastMessage("§c[Event Dragon] §fTop Damage Dealer: " + (topPlayer != null ? topPlayer.getName() : "Unknown") + " with " + sortedScores.get(topPlayer.getUniqueId()) + " damage.");
            Bukkit.broadcastMessage("§c[Event Dragon] §fSecond Place: " + (secondPlayer != null ? secondPlayer.getName() : "Unknown") + " with " + sortedScores.get(secondPlayer.getUniqueId()) + " damage.");
            Bukkit.broadcastMessage("§c[Event Dragon] §fThird Place: " + (thirdPlayer != null ? thirdPlayer.getName() : "Unknown") + " with " + sortedScores.get(thirdPlayer.getUniqueId()) + " damage.");

        } else {
            Bukkit.broadcastMessage("§c[Event Dragon] §fNo players dealt damage to the Event Dragon.");
        }

        playerDamageMap.clear();
    }

    public void respawnHealBecons() {
        for (Location loc : beaconLocations) {
            loc.getWorld().getBlockAt(loc).setType(org.bukkit.Material.BEACON);
            Bukkit.getLogger().info("Placed beacon at " + loc.toString());
        }
    }

    private void addBeaconLocations() {
        //TODO: Get beacon locations from server and add them
        World world = Bukkit.getWorld("world_the_end");
        beaconLocations.add(new Location(world, 0, 128, 0));
        beaconLocations.add(new Location(world, 100, 128, 100));
    }

    private boolean isEventDragon(EnderDragon dragon) {
        return dragon.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN);
    }

    public void clearPlayerDamageMap() {
        playerDamageMap.clear();
    }

}
