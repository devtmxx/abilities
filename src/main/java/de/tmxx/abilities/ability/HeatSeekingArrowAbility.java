package de.tmxx.abilities.ability;

import com.google.inject.Inject;
import de.tmxx.abilities.ability.heatseekingarrow.ArrowGuidance;
import de.tmxx.abilities.ability.heatseekingarrow.TargetFinder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Project: abilities
 * 12.03.25
 *
 * <p>
 *     This ability allows players to shoot arrows that lock in on targets.
 * </p>
 *
 * @author timmauersberger
 * @version 1.0
 */
public class HeatSeekingArrowAbility implements Ability, Listener, Runnable {
    private final JavaPlugin plugin;
    private final AbilityFactory factory;
    private final Map<UUID, TargetFinder> trackingPlayers = new HashMap<>();

    @Inject
    HeatSeekingArrowAbility(JavaPlugin plugin, AbilityFactory factory) {
        this.plugin = plugin;
        this.factory = factory;
    }

    @Override
    public void register() {
        // Register the ability as an event listener and schedule its tracking updates
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 5L);
    }

    /**
     * Handles player interactions to initiate target tracking when a bow is used.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.BOW)) return;
        if (trackingPlayers.containsKey(event.getPlayer().getUniqueId())) return;

        // Start tracking the target for the player
        TargetFinder targetFinder = factory.newTargetFinder(event.getPlayer());
        targetFinder.startTracking();
        trackingPlayers.put(event.getPlayer().getUniqueId(), targetFinder);
    }

    /**
     * Handles projectile launch events to assign guided targeting to arrows.
     */
    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        // Retrieve the player's target, if any
        TargetFinder targetFinder = trackingPlayers.get(player.getUniqueId());
        if (targetFinder == null) return;

        Entity target = targetFinder.getCurrentTarget();
        if (target == null) return;

        // Create an ArrowGuidance instance to control the arrow's homing behavior
        ArrowGuidance arrowGuidance = factory.newArrowGuidance(target, arrow);
        arrowGuidance.start();
    }

    /**
     * Periodic update loop to manage tracking players and clean up inactive entries.
     */
    @Override
    public void run() {
        Iterator<Map.Entry<UUID, TargetFinder>> iterator = trackingPlayers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, TargetFinder> entry = iterator.next();

            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                // Remove offline players and stop tracking
                entry.getValue().stopTracking();
                iterator.remove();
                continue;
            }

            // Stop tracking if the player is no longer using a bow
            ItemStack activeItem = player.getActiveItem();
            if (activeItem.getType().equals(Material.BOW)) return;

            entry.getValue().stopTracking();
            iterator.remove();
        }
    }
}
