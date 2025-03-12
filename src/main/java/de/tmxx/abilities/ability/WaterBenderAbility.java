package de.tmxx.abilities.ability;

import com.google.inject.Inject;
import de.tmxx.abilities.ability.waterbender.WaterQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Project: abilities
 * 08.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class WaterBenderAbility implements Ability, Listener, Runnable {
    private final Map<UUID, WaterQueue> queues = new HashMap<>();

    private final JavaPlugin plugin;
    private final AbilityFactory abilityFactory;

    @Inject
    WaterBenderAbility(JavaPlugin plugin, AbilityFactory abilityFactory) {
        this.plugin = plugin;
        this.abilityFactory = abilityFactory;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            if (queues.containsKey(event.getPlayer().getUniqueId())) {
                queues.remove(event.getPlayer().getUniqueId()).despawn();
            } else {
                Location targetLocation = locationFromView(event.getPlayer().getEyeLocation());
                if (!targetLocation.getBlock().getType().equals(Material.WATER)) return;

                WaterQueue queue = abilityFactory.newWaterQueue();
                queue.spawn(locationFromView(event.getPlayer().getEyeLocation()));
                queues.put(event.getPlayer().getUniqueId(), queue);
            }
        }
    }

    @Override
    public void run() {
        queues.forEach((uniqueId, queue) -> {
            Player player = Bukkit.getPlayer(uniqueId);
            if (player == null) return;

            Location location = locationFromView(player.getEyeLocation());
            queue.addRoute(location);
        });
    }

    @Override
    public void register() {
        Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 1L);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private Location locationFromView(Location location) {
        int radius = 10;
        Vector direction = location.getDirection().clone().normalize();
        direction.multiply(radius);
        return location.clone().add(direction);
    }
}
