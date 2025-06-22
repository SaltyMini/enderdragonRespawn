package Clay.Sam.enderdragonRespawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;


public class DragonEvents implements Listener {

    private final double damageMultiplier = 1.5;
    private DragonDamageTrack dragonDamageTrack;


    private Plugin plugin;

    public DragonEvents(Plugin plugin, DragonDamageTrack dragonDamageTrack) {

        this.plugin = plugin;
        this.dragonDamageTrack = dragonDamageTrack;

    }

    @EventHandler
    public void onDragonDamagePlayer(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getDamager();
        if (!DragonAbilities.isEventDragon(dragon)) return;

        // Your custom code when event dragon attacks
        Bukkit.getLogger().info("Event Dragon attacked " + event.getEntity().getName());

        // Example: Increase damage
        event.setDamage(event.getDamage() * damageMultiplier);

        // Example: Special effects
        event.getEntity().getLocation().getWorld().createExplosion(
                event.getEntity().getLocation(), 2.0f, false, false
        );
    }

    //Player damages dragon
    @EventHandler
    public void onPlayerDamageDragon(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        if (!DragonAbilities.isEventDragon(dragon)) return;

        if(!(event.getDamageSource().getCausingEntity() instanceof Player)) return;

        UUID playerUUID = event.getDamageSource().getCausingEntity().getUniqueId();
        float damage = (float) event.getDamage();



    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (!DragonAbilities.isEventDragon(dragon)) return;

        Bukkit.broadcastMessage("§6[Event Dragon] §fThe mighty Event Dragon has been slain!");

        dragon.getWorld().createExplosion(dragon.getLocation(), 5.0f, false, false);

        List<Map.Entry<String, Float>> topPlayers = dragonDamageTrack.getTopPlayers(3);

        for (int i = 0; i < topPlayers.size(); i++) {
            Map.Entry<String, Float> entry = topPlayers.get(i);
            String playerName = entry.getKey();
            Float damage = entry.getValue();
            Player player = Bukkit.getPlayer(playerName);

            // Determine position text
            String positionText = switch (i) {
                case 0 -> "Top Damage Dealer";
                case 1 -> "Second Place";
                case 2 -> "Third Place";
                default -> (i + 1) + "th Place";
            };

            // Give rewards based on position
            if (player != null) {
                switch (i) {
                    case 0 -> {
                        // TODO: Give first place reward
                        // Example: player.getInventory().addItem(new ItemStack(Material.DIAMOND, 10));
                    }
                    case 1 -> {
                        // TODO: Give second place reward
                        // Example: player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 5));
                    }
                    case 2 -> {
                        // TODO: Give third place reward
                        // Example: player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 3));
                    }
                }
            }

            // Broadcast message
            String displayName = player != null ? player.getName() : playerName;
            Bukkit.broadcastMessage("§c[Event Dragon] §f" + positionText + ": " + displayName +
                    " with " + String.format("%.1f", damage) + " damage.");
        }

    }

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

    @EventHandler
    public void onDragonPhaseChange(EnderDragonChangePhaseEvent event) {
        EnderDragon dragon = event.getEntity();
        if (!DragonAbilities.isEventDragon(dragon)) return;

        EnderDragon.Phase newPhase = event.getNewPhase();
        EnderDragon.Phase currentPhase = event.getCurrentPhase();

        // Check for perching phases
        if (newPhase == EnderDragon.Phase.LAND_ON_PORTAL) {
            // Dragon is about to perch or is perching
            Bukkit.getLogger().info("Event Dragon is perching!");
            Bukkit.broadcastMessage("§canceling dragon perch");
            event.setCancelled(true);
        }

        Bukkit.getLogger().info("Event Dragon phase changed from " +
                (currentPhase != null ? currentPhase.name() : "null") +
                " to " + newPhase.name());
    }

    @EventHandler
    public void onDragonTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (!DragonAbilities.isEventDragon(dragon)) return;

        Bukkit.getLogger().info("Event Dragon is targeting " +
                (event.getTarget() != null ? event.getTarget().getName() : "no one"));

        if (event.getTarget() != null) {
            Bukkit.broadcastMessage("§c[Event Dragon] §fThe Event Dragon has targeted " +
                    event.getTarget().getName() + "!");
        }
    }

}
