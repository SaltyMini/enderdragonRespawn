package Clay.Sam.enderdragonRespawn;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
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

    static DragonEvents instance = null;

    private final DragonDamageTrack dragonDamageTrack;
    private final DragonAbilities dragonAbilities;

    private static BukkitTask dragonRunnableTask;

    private static Plugin plugin;

    public DragonEvents() {

        plugin = EnderdragonRespawn.getPlugin();
        this.dragonDamageTrack = DragonDamageTrack.getInstance();
        this.dragonAbilities = DragonAbilities.getInstance();

    }

    public static DragonEvents getInstance() {
        if(instance == null) {
            instance = new DragonEvents();
        }
        return instance;
    }


    @EventHandler
    public void onDragonDamagePlayer(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof EnderDragon dragon)) return;

        if (!(DragonMob.isEventDragon(dragon))) return;

        // Your custom code when event dragon attacks
        plugin.getLogger().info("Event Dragon attacked " + event.getEntity().getName());

        // Example: Increase damage
        double damageMultiplier = 1.5;
        event.setDamage(event.getDamage() * damageMultiplier);

        // Example: Special effects
        event.getEntity().getLocation().getWorld().createExplosion(
                event.getEntity().getLocation(), 2.0f, false, false
        );
    }

    //Player damages dragon
    @EventHandler
    public void onPlayerDamageDragon(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof EnderDragon dragon)) return;
        if(!(DragonMob.isEventDragon(dragon))) return;
        if(!(event.getDamager() instanceof Player player)) return;

        float damage = (float) event.getDamage();

        dragonDamageTrack.playerDamageDragonAdd(player.getName(), damage);

        updateDragonPhase(dragon);

    }

    private void updateDragonPhase(EnderDragon dragon) {
        if(dragon.getHealth() == 0) return; // No need to update phase if dragon is dead
        if(dragon.getAttribute(Attribute.MAX_HEALTH) == null) return;
        double healthPercentage = (dragon.getHealth() / (Objects.requireNonNull(dragon.getAttribute(Attribute.MAX_HEALTH)).getBaseValue())) * 100;
        int currentPhase = dragonAbilities.getDragonPhase();

        if (healthPercentage <= 66 && healthPercentage > 33 && currentPhase == 1) {
            plugin.getLogger().info("Event Dragon entering phase 2 at " + String.format("%.1f", healthPercentage) + "% health");
            dragonAbilities.increaseDragonPhase();
        } else if (healthPercentage <= 33 && currentPhase == 2) {
            plugin.getLogger().info("Event Dragon entering phase 3 at " + String.format("%.1f", healthPercentage) + "% health");
            dragonAbilities.increaseDragonPhase();
        }
    }


    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        if (!(DragonMob.isEventDragon(dragon))) return;

        Bukkit.broadcast(Component.text("[Event Dragon] The mighty Event Dragon has been slain!")
                .color(NamedTextColor.GOLD));


        dragon.getWorld().createExplosion(dragon.getLocation(), 5.0f, false, false);

        List<Map.Entry<String, Float>> topPlayers = dragonDamageTrack.getTopPlayers(3);

        for (int i = 0; i < topPlayers.size(); i++) {
            Map.Entry<String, Float> entry = topPlayers.get(i);
            String playerName = entry.getKey();
            Float damage = entry.getValue();
            Player player = Bukkit.getPlayer(playerName);

            String positionText = getPositionText(i); //method below

            Loot.LootItem rewardItem = null;

            if (player != null) {
                switch (i) {
                    case 0 -> rewardItem = Loot.pickRandomLoot(Loot.FIRST_PLACE);
                    case 1 -> rewardItem = Loot.pickRandomLoot(Loot.SECOND_PLACE);
                    case 2 -> rewardItem = Loot.pickRandomLoot(Loot.THIRD_PLACE);
                }
                    if(rewardItem == null) {
                        plugin.getLogger().warning("No reward item found for player: " + playerName);
                        continue; // Skip if no reward item is found
                    }

                    ItemStack item = new ItemStack(rewardItem.material(), rewardItem.amount());

                    if(player.getInventory().firstEmpty() == -1) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    } else {
                        player.getInventory().addItem(item);
                    }
            }

            // Broadcast message
            String displayName = player != null ? player.getName() : playerName;
            Bukkit.broadcast(Component.text()
                    .append(Component.text("[Event Dragon] ").color(NamedTextColor.RED))
                    .append(Component.text(positionText + ": " + displayName + " with " +
                            String.format("%.1f", damage) + " damage.").color(NamedTextColor.WHITE))
                    .build());

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

    private String getPositionText(int position) {
        return switch (position) {
            case 0 -> "Top Damage Dealer";
            case 1 -> "Second Place";
            case 2 -> "Third Place";
            default -> (position + 1) + "th Place";
        };
    }

    //Increase damage done by dragon breath
    @EventHandler
    public void onBreathDmg(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        if (!(DragonMob.isEventDragon(dragon))) return;
        if (event.getEntity() instanceof Player) {
            event.setDamage(event.getDamage() * 2);
        }
    }

    @EventHandler
    public void onDragonPhaseChange(EnderDragonChangePhaseEvent event) {
        EnderDragon dragon = event.getEntity();
        if (!(DragonMob.isEventDragon(dragon))) return;

        EnderDragon.Phase newPhase = event.getNewPhase();
        EnderDragon.Phase currentPhase = event.getCurrentPhase();

        // Check for perching phases
        if (newPhase == EnderDragon.Phase.LAND_ON_PORTAL) {
            // Dragon is about to perch or is perching
            plugin.getLogger().info("Event Dragon is perching!");
            event.setCancelled(true);
            dragon.setPhase(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET);
        }

        if (newPhase == EnderDragon.Phase.FLY_TO_PORTAL) {
            // Dragon is about to perch or is perching
            plugin.getLogger().info("Event Dragon is perching!");
            event.setCancelled(true);
            dragon.setPhase(EnderDragon.Phase.CHARGE_PLAYER);
        }

        plugin.getLogger().info("Event Dragon phase changed from " +
                (currentPhase != null ? currentPhase.name() : "null") +
                " to " + newPhase.name());
    }

    @EventHandler
    public void onDragonTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        if (!(DragonMob.isEventDragon(dragon))) return;

        plugin.getLogger().info("Event Dragon is targeting " +
                (event.getTarget() != null ? event.getTarget().getName() : "no one"));

        if (event.getTarget() != null) {
            Bukkit.broadcast(Component.text()
                    .append(Component.text("[Event Dragon] ").color(NamedTextColor.RED))
                    .append(Component.text("The Event Dragon has targeted " +
                            event.getTarget().getName() + "!").color(NamedTextColor.WHITE))
                    .build());

        }
    }



    public static synchronized void StartDragonMobRunnable() {
        StopDragonMobRunnable();
    
        DragonMobRunnable dragonMobRunnable = new DragonMobRunnable();
        dragonRunnableTask = Bukkit.getScheduler().runTaskTimer(plugin, dragonMobRunnable, 0L, 20L);
    
        plugin.getLogger().info("DragonMobRunnable started.");
    }

    public static synchronized void StopDragonMobRunnable() {
        if (dragonRunnableTask != null && !dragonRunnableTask.isCancelled()) {
            dragonRunnableTask.cancel();
            dragonRunnableTask = null;
            plugin.getLogger().info("Dragon runnable stopped!");
        }
    }
    
    public static class DragonMobRunnable implements Runnable {


        @Override
        public void run() {


            World endWorld = Bukkit.getWorld("world_the_end");
            if (endWorld == null) return;


            int dragonPhase = DragonAbilities.getInstance().getDragonPhase();
            int abilityRate = Math.max(1, dragonPhase) * 50;

            Random random = new Random();

            EnderDragon eventDragon = null;
            for (EnderDragon dragon : endWorld.getEntitiesByClass(EnderDragon.class)) {
                if (DragonMob.isEventDragon(dragon)) {
                    eventDragon = dragon;
                    break;
                }
            }
            if (eventDragon == null) return;


            if (eventDragon.getTarget() == null) {
                for (Player player : endWorld.getPlayers()) {
                    double distance = player.getLocation().distance(eventDragon.getLocation());
                    if (distance < 128) { // Within 128 blocks
                        eventDragon.setTarget(player);
                        break;
                    }
                }
            }


            // 1 over abilityRate chance to run an ability
            if(random.nextInt(abilityRate) == 0) {

                int ability = random.nextInt(3);

                switch (ability) {
                    case 0 -> {
                        plugin.getLogger().info("Event Dragon is using respawn heal beacons ability!");
                        DragonAbilities.getInstance().respawnHealBeaconsAbility();
                    }
                    case 1 -> {
                        plugin.getLogger().info("Event Dragon is using spawn minions ability!");
                        DragonAbilities.getInstance().spawnMinionsAbility();
                    }
                    case 2 -> {
                        plugin.getLogger().info("Event Dragon is using angry endermen ability!");
                        DragonAbilities.getInstance().angryEnderman();
                    }
                }

            }

        }
    }
}