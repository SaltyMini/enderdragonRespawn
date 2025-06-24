package Clay.Sam.enderdragonRespawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class DragonMob implements Listener {

    private static Plugin plugin;
    private static BukkitTask dragonRunnableTask;


    public DragonMob(Plugin plugin) {
        this.plugin = plugin;

    }

    public static void spawnDragon(   ) {

        final double healthMultiplier = 3.0;
        World world = Bukkit.getWorld("world_the_end");

        if (world == null) {
            Bukkit.getLogger().warning("World 'world_the_end' not found.");
            return; // World not found
        }

        Location spawnLocation = new Location(world, 0, 128, 0);

        EnderDragon dragon = (EnderDragon) world.spawnEntity(spawnLocation, EntityType.ENDER_DRAGON);
        dragon.setHealth(dragon.getMaxHealth() * healthMultiplier);

        NamespacedKey key = new NamespacedKey(plugin, "eventDragon");
        dragon.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        // Optional: Also add scoreboard tag for command compatibility
        dragon.addScoreboardTag("eventDragon");

        StartDragonMobRunnable();
    }

    public static void killExistingEventDragons() {
        World endWorld = Bukkit.getWorld("world_the_end");

        if (endWorld == null) {
            Bukkit.getLogger().warning("World 'world_the_end' not found during startup cleanup.");
            return;
        }

        int killedCount = 0;
        for (Entity entity : endWorld.getEntitiesByClass(EnderDragon.class)) {
            if (entity instanceof EnderDragon) {
                EnderDragon dragon = (EnderDragon) entity;

                // Check if this is an event dragon using the scoreboard tag
                if (dragon.getScoreboardTags().contains("eventDragon")) {
                    dragon.remove();
                    killedCount++;
                }
            }
        }

        DragonDamageTrack.clearPlayerDamageMap();
        Bukkit.getLogger().info("Removed " + killedCount + " existing event dragon(s) on startup.");

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

    public static class DragonMobRunnable implements Runnable {

        @Override
        public void run() {

            Bukkit.getLogger().info("runs every second");

        }

    }



}
