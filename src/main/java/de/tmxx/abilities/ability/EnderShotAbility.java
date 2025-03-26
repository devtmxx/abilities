package de.tmxx.abilities.ability;

import com.google.inject.Inject;
import de.tmxx.abilities.ability.endershot.EnderShotProjectile;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Project: abilities
 * 07.03.2025
 *
 * <p>
 *     When a player shoots with a crossbow this ability intercepts and launches a custom projectile.
 * </p>
 *
 * @author timmauersberger
 * @version 1.0
 */
public class EnderShotAbility implements Ability, Listener {
    private final JavaPlugin plugin;
    private final AbilityFactory abilityFactory;

    @Inject
    EnderShotAbility(JavaPlugin plugin, AbilityFactory abilityFactory) {
        this.plugin = plugin;
        this.abilityFactory = abilityFactory;
    }

    @Override
    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (!player.getInventory().getItemInMainHand().getType().equals(Material.CROSSBOW)) return;

        event.getEntity().remove();

        EnderShotProjectile projectile = abilityFactory.newEnderShotProjectile(player);
        projectile.launch();
    }
}
