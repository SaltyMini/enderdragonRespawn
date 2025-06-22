package Clay.Sam.enderdragonRespawn;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PvpEvent implements Listener {

    @EventHandler
    public void onPvP(EntityDamageEvent event) {

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getWorld().getName().equalsIgnoreCase("world_the_end")) {
                Location playerLoc = player.getLocation();
                if (playerLoc.getY() > 100 || playerLoc.getY() < -100) {
                    if (playerLoc.getX() > 100 || playerLoc.getX() < -100) {

                        event.setCancelled(true);
                        player.sendMessage("§cYou cannot PvP on the main end island");

                    }
                }
            }

        }

    }
}
