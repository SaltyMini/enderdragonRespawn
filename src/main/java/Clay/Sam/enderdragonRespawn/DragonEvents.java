package Clay.Sam.enderdragonRespawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DragonEvents implements Listener {

    private final double damageMultiplier = 1.5;
    private DragonDamageTrack dragonDamageTrack;
    private DragonAbilities dragonAbilities;

    private static BukkitTask dragonRunnableTask;

    private static Plugin plugin;

    public DragonEvents(Plugin plugin) {

        this.plugin = plugin;
        this.dragonDamageTrack = new DragonDamageTrack();
        this.dragonAbilities = new DragonAbilities(plugin, dragonDamageTrack);

    }

    @EventHandler
    public void onDragonDamagePlayer(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getDamager();
        if (!DragonAbilities.isEventDragon(dragon)) return;

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
    public void onPlayerDamageDragon(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        if (!DragonAbilities.isEventDragon(dragon)) return;
        if (!(event.getDamageSource().getCausingEntity() instanceof Player player)) return;

        float damage = (float) event.getDamage();

        dragonDamageTrack.playerDamageDragonAdd(player.getName(), damage);

        // change phase based on health, 100-60% phase 0 - 66-34 phase 1 - 33-0 phase 2
        if (dragon.getHealth() <= dragon.getMaxHealth() * 0.66 && dragon.getHealth() > dragon.getMaxHealth() * 0.33) {
            Bukkit.getLogger().info("Event Dragon is now in phase 1 (66-34%)");
            dragonAbilities.increaseDragonPhase();

        } else if (dragon.getHealth() <= dragon.getMaxHealth() * 0.33 && dragon.getHealth() > 0) {
            Bukkit.getLogger().info("Event Dragon is now in phase 2 (33-0%)");
            dragonAbilities.increaseDragonPhase();

        }
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (!DragonAbilities.isEventDragon(dragon)) return;

        Bukkit.broadcastMessage("§6[Event Dragon] §fThe mighty Event Dragon has been slain!");

        dragon.getWorld().createExplosion(dragon.getLocation(), 5.0f, false, false);

        List<Map.Entry<String, Float>> topPlayers = dragonDamageTrack.getTopPlayers(3);

        for (int i = 0; i < topPlayers.size(); i++) {
            Map.Entry<String, Float> entry = topPlayers.get(i);
            String playerName = entry.getKey();
            Float damage = entry.getValue();
            Player player = Bukkit.getPlayer(playerName);

            // Determine position text
            String positionText = switch (i) {
                case 0 -> "Top Damage Dealer";
                case 1 -> "Second Place";
                case 2 -> "Third Place";
                default -> (i + 1) + "th Place";
            };

            Loot.LootItem rewardItem = null;

            if (player != null) {
                switch (i) {
                    case 0 -> {
                        rewardItem = Loot.pickRandomLoot(Loot.FIRST_PLACE);
                        ItemStack item = new ItemStack(rewardItem.material(), rewardItem.amount());

                        player.getInventory().addItem(item);
                    }
                    case 1 -> {
                        rewardItem = Loot.pickRandomLoot(Loot.SECOND_PLACE);
                        ItemStack item = new ItemStack(rewardItem.material(), rewardItem.amount());

                        player.getInventory().addItem(item);
                    }
                    case 2 -> {
                        rewardItem = Loot.pickRandomLoot(Loot.THIRD_PLACE);
                        ItemStack item = new ItemStack(rewardItem.material(), rewardItem.amount());

                        player.getInventory().addItem(item);
                    }
                }
            }


            // Broadcast message
            String displayName = player != null ? player.getName() : playerName;
            Bukkit.broadcastMessage("§c[Event Dragon] §f" + positionText + ": " + displayName +
                    " with " + String.format("%.1f", damage) + " damage.");
        }

        StopDragonMobRunnable();
    }

    /* might want to use later
    @EventHandler
    public void onDragonProjectile(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getEntity().getShooter();
        if (!DragonAbilities.isEventDragon(dragon)) return;

        Bukkit.getLogger().info("Event Dragon launched a fireball!");

        // Example: Make fireballs faster
        event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(2.0));

        // Example: Spawn multiple fireballs
        for (int i = 0; i < 2; i++) {
            dragon.getWorld().spawn(dragon.getLocation(),
                    event.getEntity().getClass());
        }
    }
     */

    //Increase damage done by dragon breath
    @EventHandler
    public void onBreathDmg(EntityDamageEvent event) {
        if(event.getCause() != EntityDamageEvent.DamageCause.DRAGON_BREATH) return;
        if (event.getEntity() instanceof Player) {
            event.setDamage(event.getDamage() * 2);
        };
    }

    @EventHandler
    public void onDragonPhaseChange(EnderDragonChangePhaseEvent event) {
        EnderDragon dragon = event.getEntity();
        if (!DragonAbilities.isEventDragon(dragon)) return;

        EnderDragon.Phase newPhase = event.getNewPhase();
        EnderDragon.Phase currentPhase = event.getCurrentPhase();

        // Check for perching phases
        if (newPhase == EnderDragon.Phase.LAND_ON_PORTAL) {
            // Dragon is about to perch or is perching
            Bukkit.getLogger().info("Event Dragon is perching!");
            Bukkit.broadcastMessage("§canceling dragon perch");
            event.setCancelled(true);
        }

        Bukkit.getLogger().info("Event Dragon phase changed from " +
                (currentPhase != null ? currentPhase.name() : "null") +
                " to " + newPhase.name());
    }

    @EventHandler
    public void onDragonTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (!DragonAbilities.isEventDragon(dragon)) return;

        Bukkit.getLogger().info("Event Dragon is targeting " +
                (event.getTarget() != null ? event.getTarget().getName() : "no one"));

        if (event.getTarget() != null) {
            Bukkit.broadcastMessage("§c[Event Dragon] §fThe Event Dragon has targeted " +
                    event.getTarget().getName() + "!");
        }
    }



    public static void StartDragonMobRunnable() {
        StopDragonMobRunnable();

        DragonMobRunnable dragonMobRunnable = new DragonMobRunnable();
        dragonRunnableTask = Bukkit.getScheduler().runTaskTimer(plugin, dragonMobRunnable, 0L, 20L); // Runs every second

        Bukkit.getLogger().info("DragonMobRunnable started.");
    }

    public static void StopDragonMobRunnable() {
        if (dragonRunnableTask != null && !dragonRunnableTask.isCancelled()) {
            dragonRunnableTask.cancel();
            dragonRunnableTask = null;
            Bukkit.getLogger().info("Dragon runnable stopped!");
        }
    }


    //TODO: Add abilities to runnable
    //TODO: Add instance get methods
    public static class DragonMobRunnable implements Runnable {

        @Override
        public void run() {

            Bukkit.getLogger().info("runs every second");
            int dragonPhase = DragonAbilities.getDragonPhase();

            switch (dragonPhase) {
                case 1:
                    ;
                    break;
            }

        }


    //award tables

    public Material[] getFirstPlaceRewards() {
        return new Material[]{
                Material.DIAMOND,
                Material.NETHERITE_INGOT,
                Material.ENCHANTED_GOLDEN_APPLE
        };
    }

}
