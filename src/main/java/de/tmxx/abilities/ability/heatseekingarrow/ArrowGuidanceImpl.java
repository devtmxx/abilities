package de.tmxx.abilities.ability.heatseekingarrow;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
    private final Player shooter;
    private final Entity target;
    private final Arrow arrow;

    @Inject
    ArrowGuidanceImpl(JavaPlugin plugin, @Assisted Player shooter, @Assisted Entity target, @Assisted Arrow arrow) {
        this.plugin = plugin;
        this.shooter = shooter;
        this.target = target;
        this.arrow = arrow;
    }

    @Override
    public void run() {
        if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround() || target.isDead() || !target.isValid()) {
            cancel();
            return;
        }

        Vector currentVelocity = arrow.getVelocity();
        double speed = currentVelocity.length();
        Vector targetDirection = target.getLocation().toVector().subtract(arrow.getLocation().toVector()).normalize();
        Vector newVelocity = currentVelocity.multiply(0.5).add(targetDirection.multiply(0.5)).normalize().multiply(speed);

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
        runTaskTimer(plugin, 0, 1L);
    }
}
