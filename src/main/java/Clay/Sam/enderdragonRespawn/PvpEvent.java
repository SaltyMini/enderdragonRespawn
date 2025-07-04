package Clay.Sam.enderdragonRespawn;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.Plugin;


public class PvpEvent implements Listener {

    Plugin plugin;
    World world;

    public PvpEvent() {
        plugin = EnderdragonRespawn.getPlugin();
        world = EnderdragonRespawn.getWorld();
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player player)) return;


        if (player.getWorld() == world) {
                Location playerLoc = player.getLocation();
                if (Math.abs(playerLoc.getX()) < 100 && Math.abs(playerLoc.getZ()) < 100) {
                    event.setCancelled(true);
                    player.sendMessage("§cYou cannot PvP on the main end island");
                }
            }
    }

    //Disable fly on main island
    @EventHandler
    public void cancelFly(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        player.getGameMode();
        if(player.getGameMode() != GameMode.SURVIVAL) return;
        if (player.getWorld() == EnderdragonRespawn.getWorld()) {
            Location playerLoc = player.getLocation();
            if (Math.abs(playerLoc.getX()) < 100 && Math.abs(playerLoc.getZ()) < 100) {
                player.setAllowFlight(false);
                player.setFlying(false);
                event.setCancelled(true);
                player.sendMessage("§cYou cannot fly on the main end island");
            }
        }
    }

    //Apply scoreboard to playres on join
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        DragonMob.applyScoreboardPlayer(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        DragonMob.removeScoreboardPlayer(event.getPlayer());
    }
}