package Clay.Sam.enderdragonRespawn;

import org.bukkit.*;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.attribute.AttributeInstance;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

public class DragonMob implements Listener {

    private static Plugin plugin;
    public static DragonMob instance = null;
    private static World world;
    private static BossBar eventDragonBossBar;
    private static Scoreboard scoreboard;
    private static Objective objective;

    private static AttributeInstance maxHealthAttribute;


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


        DragonAbilities.getInstance().resetDragonPhase();
        dragon.setAI(true);
        dragon.setPhase(EnderDragon.Phase.CIRCLING);

        DragonAbilities.getInstance().respawnHealBeaconsAbility();

        createBossBar();
        createScoreboard();

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
        if(world == null) {
            plugin.getLogger().warning("World 'world_the_end' not found when trying to get event dragon.");
            return null; // World not found
        }
        for(EnderDragon dragon : world.getEntitiesByClass(EnderDragon.class)) {
            if (isEventDragon(dragon)) {
                return dragon;
            }
        }
        return null;
    }

    public static void createBossBar() {
        if(eventDragonBossBar != null) {
            eventDragonBossBar.removeAll();
            eventDragonBossBar.setVisible(false);
        }

        eventDragonBossBar = Bukkit.createBossBar("§c§lEvent Dragon", org.bukkit.boss.BarColor.RED, BarStyle.SEGMENTED_20);

        eventDragonBossBar.setProgress(1.0);

        for(Player player : plugin.getServer().getOnlinePlayers()) {
            eventDragonBossBar.addPlayer(player);
        }

        eventDragonBossBar.setVisible(true);
    }

    //
     // // Boss Bar Methods
     //

    public static void updateBossBar() {
        EnderDragon dragon = getEventDragon();
        if (dragon == null || eventDragonBossBar == null) return;

        if(maxHealthAttribute == null) {
            maxHealthAttribute = dragon.getAttribute(Attribute.MAX_HEALTH);
        }
        
        if(maxHealthAttribute == null) return;
        
        double health = dragon.getHealth();
        double maxHealth = maxHealthAttribute.getValue();

        eventDragonBossBar.setProgress(Math.max(0.0, health / maxHealth));
        eventDragonBossBar.setTitle("§c§lEvent Dragon - " + (int) health + " / " + (int) maxHealth + " HP");
    }

    public static void removeBossBarPlayer(Player player) {
        if(eventDragonBossBar != null) {
            eventDragonBossBar.removePlayer(player);
        }
    }

    public static void removeBossBar() {
        if(eventDragonBossBar != null) {
            eventDragonBossBar.removeAll();
            eventDragonBossBar = null;
        }
    }

    public static void showBossBarPlayer(Player player) {
        if(eventDragonBossBar != null) {
            eventDragonBossBar.addPlayer(player);
        }
    }

    //
     // Scoreboard Methods
    //

    public static void createScoreboard() {
        // Remove existing scoreboard first
        if(scoreboard != null) {
            removeScoreboard();
        }

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("eventDragon", "dummy", "§c§lEvent Dragon");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Set header
        objective.getScore("§c§l   Top Damage     ").setScore(6);
        
        // Set unique empty placeholders
        objective.getScore("§7      -- ").setScore(5);
        objective.getScore("§7      --  ").setScore(4);  
        objective.getScore("§7      --   ").setScore(3);
        objective.getScore("§7      --    ").setScore(2);
        objective.getScore("§7      --     ").setScore(1);

        // Apply to all online players
        for(Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

    public static void updateScoreboard() {
        if (objective == null || scoreboard == null) return;
        
        List<Map.Entry<String, Float>> topPlayers = DragonDamageTrack.getInstance().getTopPlayers(5);
        
        // Clear all existing entries except the header - SAFE way to avoid ConcurrentModificationException
        Set<String> entriesToRemove = new HashSet<>(scoreboard.getEntries());
        entriesToRemove.remove("§c§l   Top Damage     ");
        for(String entry : entriesToRemove) {
            scoreboard.resetScores(entry);
        }

        
        // Add scores for actual players and fill remaining slots with placeholders
        for (int i = 0; i < 5; i++) {
            if(topPlayers.get(i) != null) {
                plugin.getLogger().info("Updating scoreboard for player " + i + " - " + topPlayers.get(i).getKey());
            } else {
                plugin.getLogger().info("size: " + topPlayers.size());
            }

            String display;
            if (i < topPlayers.size()) {
                Map.Entry<String, Float> player = topPlayers.get(i);
                display = "§6" + (i + 1) + ". " + player.getKey() + ": " + String.format("%.1f", player.getValue());
            } else {
                // Fill remaining slots with unique empty entries
                display = "§7      --" + " ".repeat(i + 1);
            }
            objective.getScore(display).setScore(5 - i);
        }
    }

    public static void removeScoreboard() {
        if(scoreboard != null) {
            for(Player player : Bukkit.getOnlinePlayers()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
            scoreboard = null;
            objective = null;
        }
    }

    public static void removeScoreboardPlayer(Player player) {
        if (scoreboard != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public static void applyScoreboardPlayer(Player player) {
        if(scoreboard != null) {
            player.setScoreboard(scoreboard);
        }
    }

    public static void dragonRawAll() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld() == world) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
            }
        }
    }
}