package Clay.Sam.enderdragonRespawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.attribute.Attribute;

import java.util.Objects;

public class DragonMob implements Listener {

    private static Plugin plugin;
    public static DragonMob instance = null;
    static World world;

    public DragonMob() {
        plugin = EnderdragonRespawn.getPlugin();
        world = EnderdragonRespawn.getWorld();
    }

    public static synchronized DragonMob getInstance() {
        if (instance == null) {
            instance = new DragonMob();
        }
        return instance;
    }

    public static void spawnDragon(   ) {

        final double healthMultiplier = 3.0;
        World world = Bukkit.getWorld("world_the_end");

        if (world == null) {
            plugin.getLogger().warning("World 'world_the_end' not found.");
            return; // World not found
        }

        Location spawnLocation = new Location(world, 0, 128, 0);

        EnderDragon dragon = (EnderDragon) world.spawnEntity(spawnLocation, EntityType.ENDER_DRAGON);
        if (dragon.getAttribute(Attribute.MAX_HEALTH) == null) return;
        Objects.requireNonNull(dragon.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(200 * healthMultiplier);
        dragon.setHealth(200 * healthMultiplier);



        NamespacedKey key = new NamespacedKey(plugin, "eventDragon");
        dragon.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        dragon.setCustomName("§c§lEvent Dragon");
        dragon.setCustomNameVisible(true);


        DragonAbilities.getInstance().resetDragonPhase();
        dragon.setAI(true);
        dragon.setPhase(EnderDragon.Phase.CIRCLING);

        DragonAbilities.getInstance().respawnHealBeaconsAbility();

        if(!world.getPlayers().isEmpty()) {
            DragonEvents.StartDragonMobRunnable();
        }
    }

    public static void killExistingEventDragons() {
        World endWorld = Bukkit.getWorld("world_the_end");

        if (endWorld == null) {
            plugin.getLogger().warning("World 'world_the_end' not found during startup cleanup.");
            return;
        }

        int killedCount = 0;
        for (EnderDragon entity : endWorld.getEntitiesByClass(EnderDragon.class)) {
            // Check if this is an event dragon using the scoreboard tag
                if (DragonMob.isEventDragon(entity)) {
                    entity.remove();
                    killedCount++;
                }
        }

        DragonDamageTrack.getInstance().clearPlayerDamageMap();
        plugin.getLogger().info("Removed " + killedCount + " existing event dragon(s) on startup.");

    }

    public static boolean isEventDragon(EnderDragon dragon) {
        NamespacedKey key = new NamespacedKey(plugin, "eventDragon");
        return dragon.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN);
    }

    public static EnderDragon getEventDragon() {
        for(EnderDragon dragon : world.getEntitiesByClass(EnderDragon.class)) {
            if (isEventDragon(dragon)) {
                return dragon;
            }
        }
        return null;
    }

}
