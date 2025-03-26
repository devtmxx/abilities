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
 * <p>
 *     This ability enables players to summon and control water using right-click interactions.
 *     If a player right-clicks while looking at water, they can summon a water stream. Right-clicking
 *     again will remove the stream and place real water blocks.
 * </p>
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

    /**
     * Handles player interactions to summon or remove water streams.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            UUID uniqueId = event.getPlayer().getUniqueId();

            // Remove existing water stream if already active
            if (queues.containsKey(uniqueId)) {
                queues.remove(uniqueId).despawn();
            } else {
                Location targetLocation = locationFromView(event.getPlayer().getEyeLocation());

                // Only activate ability if looking at water
                if (!targetLocation.getBlock().getType().equals(Material.WATER)) return;

                WaterQueue queue = abilityFactory.newWaterQueue();
                queue.spawn(locationFromView(event.getPlayer().getEyeLocation()));
                queues.put(uniqueId, queue);
            }
        }
    }

    /**
     * Periodically updates the route of active water streams based on player movement.
     */
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
        // Register the ability as an event listener and schedule its updates
        Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 1L);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Calculates the target location on the player's line of sight.
     *
     * @param location The player's eye location.
     * @return The projected target location within a fixed radius.
     */
    private Location locationFromView(Location location) {
        int radius = 10;
        Vector direction = location.getDirection().clone().normalize();
        direction.multiply(radius);
        return location.clone().add(direction);
    }
}
