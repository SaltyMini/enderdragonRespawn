package Clay.Sam.enderdragonRespawn;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


public class PvpEvent implements Listener {

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player player)) return;


        if (player.getWorld().getName().equalsIgnoreCase("world_the_end")) {
                Location playerLoc = player.getLocation();
                if (Math.abs(playerLoc.getX()) < 100 && Math.abs(playerLoc.getZ()) < 100) {
                    event.setCancelled(true);
                    player.sendMessage("§cYou cannot PvP on the main end island");
                }
            }



    }
}