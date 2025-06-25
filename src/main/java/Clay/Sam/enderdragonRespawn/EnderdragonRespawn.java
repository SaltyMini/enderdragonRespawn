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

    private static Plugin plugin;

    @Override
    public void onEnable() {

        this.plugin = this;

        DragonMob.killExistingEventDragons();
        Bukkit.getPluginManager().registerEvents(new PvpEvent(), this);

        Bukkit.getPluginManager().registerEvents(DragonEvents.getInstance(), this);

    }

    @Override
    public void onDisable() {
        DragonMob.killExistingEventDragons();
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
                DragonDamageTrack.clearPlayerDamageMap();
                DragonMob.spawnDragon();
                commandSender.sendMessage("Event Dragon has been spawned.");
            }
            return true;
        } else if(command.getLabel().equalsIgnoreCase("killEventDragon")) {

            if (!commandSender.hasPermission("enderdragonrespawn.kill")) {
                commandSender.sendMessage("You do not have permission to use this command.");
                return true;
            }

            DragonMob.killExistingEventDragons();
            commandSender.sendMessage("All existing event dragons have been removed.");
            return true;
        }

        return true;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

}
