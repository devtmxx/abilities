package de.tmxx.abilities.ability;

import com.google.inject.Inject;
import de.tmxx.abilities.ability.heatseekingarrow.TargetFinder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
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
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 5L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.BOW)) return;
        if (trackingPlayers.containsKey(event.getPlayer().getUniqueId())) return;

        TargetFinder targetFinder = factory.newTargetFinder(event.getPlayer());
        targetFinder.startTracking();
        trackingPlayers.put(event.getPlayer().getUniqueId(), targetFinder);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;
    }

    @Override
    public void run() {
        Iterator<Map.Entry<UUID, TargetFinder>> iterator = trackingPlayers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, TargetFinder> entry = iterator.next();

            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                entry.getValue().stopTracking();
                iterator.remove();
                continue;
            }

            ItemStack activeItem = player.getActiveItem();
            if (activeItem.getType().equals(Material.BOW)) return;

            entry.getValue().stopTracking();
            iterator.remove();
        }
    }
}
