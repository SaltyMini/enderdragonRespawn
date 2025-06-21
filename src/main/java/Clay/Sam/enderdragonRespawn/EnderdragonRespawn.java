package Clay.Sam.enderdragonRespawn;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EnderdragonRespawn extends JavaPlugin implements CommandExecutor {

    private Plugin plugin;
    private DragonMob dragonMob;
    private DragonAbilities dragonAbilities;

    @Override
    public void onEnable() {

        this.plugin = this;
        dragonMob = new DragonMob(this);
        dragonAbilities = new DragonAbilities(this);

        killExistingEventDragons();
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(command.getLabel().equalsIgnoreCase("spawnEventDragon")) {

            if (!commandSender.hasPermission("enderdragonrespawn.spawn")) {
                commandSender.sendMessage("You do not have permission to use this command.");
                return true;
            }

            World endWorld = Bukkit.getWorld("world_the_end");

            if (endWorld == null) {
                Bukkit.getLogger().warning("World 'world_the_end' not found during startup cleanup.");
                return true;
            }

            int dragonCount = 0;
            for (Entity entity : endWorld.getEntities()) {
                if (entity instanceof EnderDragon) {
                    EnderDragon dragon = (EnderDragon) entity;

                    // Check if this is an event dragon using the scoreboard tag
                    if (dragon.getScoreboardTags().contains("eventDragon")) {
                        dragonCount++;
                    }
                }
            }

            if(dragonCount > 0) {
                commandSender.sendMessage("An event dragon is already present in the End.");
                commandSender.sendMessage("use /killEventDragon to remove it.");
                return true;
            } else {
                dragonAbilities.clearPlayerDamageMap();
                dragonMob.spawnDragon();
                commandSender.sendMessage("Event Dragon has been spawned.");
            }
            return true;
        } else if(command.getLabel().equalsIgnoreCase("killEventDragon")) {

            if (!commandSender.hasPermission("enderdragonrespawn.kill")) {
                commandSender.sendMessage("You do not have permission to use this command.");
                return true;
            }

            killExistingEventDragons();
            commandSender.sendMessage("All existing event dragons have been removed.");
            return true;
        }

        return true;
    }


    private void killExistingEventDragons() {
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

            dragonAbilities.clearPlayerDamageMap();
            Bukkit.getLogger().info("Removed " + killedCount + " existing event dragon(s) on startup.");

    }

    public Plugin getPlugin() {
        return plugin;
    }

}
