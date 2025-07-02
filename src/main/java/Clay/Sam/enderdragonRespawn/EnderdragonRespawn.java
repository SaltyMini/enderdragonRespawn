package Clay.Sam.enderdragonRespawn;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class EnderdragonRespawn extends JavaPlugin implements CommandExecutor {

    private static Plugin plugin;
    private static World world;

    @Override
    public void onEnable() {

        plugin = this;

        world = Bukkit.getWorld("world_the_end");
        if (world == null) {
            getLogger().warning("World 'world_the_end' not found during startup.");
        }

        Objects.requireNonNull(this.getCommand("spawnEventDragon")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("killEventDragon")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("hideEventBar")).setExecutor(this);

        DragonMob.getInstance();
        DragonAbilities.getInstance();
        DragonDamageTrack.getInstance();


        DragonMob.killExistingEventDragons();
        Bukkit.getPluginManager().registerEvents(new PvpEvent(), this);

        Bukkit.getPluginManager().registerEvents(DragonEvents.getInstance(), this);

    }

    @Override
    public void onDisable() {
        DragonEvents.StopDragonMobRunnable();
        DragonMob.killExistingEventDragons();
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if(command.getLabel().equalsIgnoreCase("spawnEventDragon")) {

            if (!commandSender.hasPermission("enderdragonrespawn.spawn")) {
                commandSender.sendMessage("You do not have permission to use this command.");
                return true;
            }

            World endWorld = Bukkit.getWorld("world_the_end");

            if (endWorld == null) {
                plugin.getLogger().warning("World 'world_the_end' not found during startup cleanup.");
                return true;
            }

            int dragonCount = 0;
            for (EnderDragon dragon : endWorld.getEntitiesByClass(EnderDragon.class)) {
                if (DragonMob.isEventDragon(dragon)) {
                    dragonCount++;
                }
            }

            if(dragonCount > 0) {
                commandSender.sendMessage("An event dragon is already present in the End.");
                commandSender.sendMessage("use /killEventDragon to remove it.");
                return true;
            } else {
                DragonDamageTrack.getInstance().clearPlayerDamageMap();
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
        } else if (command.getLabel().equalsIgnoreCase("hideEventBar")) {

            if(commandSender instanceof Player player) {
                DragonMob.removeBossBarPlayer(player);
                DragonMob.removeScoreboardPlayer(player);
                return true;
            }

        }

        return true;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static World getWorld() {
        return world;
    }

}