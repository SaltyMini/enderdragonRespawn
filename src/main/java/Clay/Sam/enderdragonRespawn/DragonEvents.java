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
import org.bukkit.event.player.PlayerChangedWorldEvent;
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

    World endWorld;
    World world;

    public DragonEvents() {

        plugin = EnderdragonRespawn.getPlugin();
        this.dragonDamageTrack = DragonDamageTrack.getInstance();
        this.dragonAbilities = DragonAbilities.getInstance();

        endWorld = Bukkit.getWorld("world_the_end");
        if (endWorld == null) {
            plugin.getLogger().warning("World 'world_the_end' not found during DragonEvents initialization.");
        }
        world = Bukkit.getWorld("world");
        if (world == null) {
            plugin.getLogger().warning("World 'world' not found during DragonEvents initialization.");
        }
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

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
            if(world.getPlayers().isEmpty()) {
                if(dragonRunnableTask != null) {
                    StopDragonMobRunnable();
                    plugin.getLogger().info("DragonMobRunnable stopped due to no players in the end.");
                }
            }


        if(world.getPlayers().size() > 0) {
            if(dragonRunnableTask == null) {
                StartDragonMobRunnable();
                plugin.getLogger().info("DragonMobRunnable started due to players in the end.");
            }
        }
    }

    public static synchronized void StartDragonMobRunnable() {
        StopDragonMobRunnable();

        if(dragonRunnableTask == null) {
            DragonMobRunnable dragonMobRunnable = new DragonMobRunnable();
            dragonRunnableTask = Bukkit.getScheduler().runTaskTimer(plugin, dragonMobRunnable, 0L, 200L);
        }

        plugin.getLogger().info("DragonMobRunnable started.");
    }

    public static synchronized void StopDragonMobRunnable() {
        if (dragonRunnableTask != null && !dragonRunnableTask.isCancelled()) {
            dragonRunnableTask.cancel();
            dragonRunnableTask = null;
            plugin.getLogger().info("Dragon runnable stopped!");
        }
    }

    //TODO: Change to every 10 seconds
    public static class DragonMobRunnable implements Runnable {

        World world;
        Random random;
        private List<Runnable> abilitiesQueue = new ArrayList<>();

        public DragonMobRunnable() {
            world = Bukkit.getWorld("world_the_end");
            if (world == null) {plugin.getLogger().warning("World 'world_the_end' not found during DragonMobRunnable initialization.");}
            if (random == null) {random = new Random();}
        }

        public boolean shouldEventHappen(int percentage, Random random) {
            return random.nextInt(100) < percentage;
        }

        @Override
        public void run() {

            //check players are still in end
            if (world.getPlayers().isEmpty()) {
                plugin.getLogger().info("No players in the end, stopping DragonMobRunnable.");
                StopDragonMobRunnable();
                return;
            }


            //
            // Queue abilities
            //
            if(abilitiesQueue.size() < 5) {
                int dragonPhase = DragonAbilities.getInstance().getDragonPhase();

                // Convert phase to percentage
                int chancePercentage = dragonPhase * 25;

                if (!(shouldEventHappen(chancePercentage, random))) {
                    return;
                }
                int rand = random.nextInt(DragonAbilities.getInstance().getAbilities().length);
                abilitiesQueue.add(DragonAbilities.getInstance().getAbilities()[rand]);
            }
            // run abilties
            try {
                Runnable ability = abilitiesQueue.get(0);
                ability.run();
                abilitiesQueue.remove(0);

            } catch (IndexOutOfBoundsException e) {
                plugin.getLogger().warning("No abilities to run, skipping this cycle.");
            } catch (Exception e) {
                plugin.getLogger().severe("Error running abilities: " + e.getMessage());

            }


        }
    }
}