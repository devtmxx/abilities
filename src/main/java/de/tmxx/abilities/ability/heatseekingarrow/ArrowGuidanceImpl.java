package de.tmxx.abilities.ability.heatseekingarrow;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Project: abilities
 * 12.03.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class ArrowGuidanceImpl extends BukkitRunnable implements ArrowGuidance, Listener {
    private final JavaPlugin plugin;
    private final Entity target;
    private final Arrow arrow;

    private double initialDistance = 0.0D;

    @Inject
    ArrowGuidanceImpl(JavaPlugin plugin, @Assisted Entity target, @Assisted Arrow arrow) {
        this.plugin = plugin;
        this.target = target;
        this.arrow = arrow;
    }

    @Override
    public void run() {
        if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround() || target.isDead() || !target.isValid()) {
            cancel();
            return;
        }

        double currentDistance = arrow.getLocation().distance(target.getLocation());
        double distanceRatio = currentDistance / initialDistance;

        Vector currentVelocity = arrow.getVelocity();
        double speed = currentVelocity.length();
        Vector targetDirection = target.getLocation().toVector().subtract(arrow.getLocation().toVector()).normalize();
        Vector newVelocity = currentVelocity.multiply(distanceRatio).add(targetDirection.multiply(1 - distanceRatio)).normalize().multiply(speed);

        arrow.setVelocity(newVelocity);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!event.getEntity().equals(arrow)) return;

        cancel();
    }

    @Override
    public void start() {
        arrow.setGravity(false);
        initialDistance = arrow.getLocation().distance(target.getLocation());
        runTaskTimer(plugin, 0, 1L);
    }
}
