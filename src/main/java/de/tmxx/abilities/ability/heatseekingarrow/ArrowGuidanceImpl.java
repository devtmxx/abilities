package de.tmxx.abilities.ability.heatseekingarrow;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.Vector3F;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.abilities.wrapper.packet.WorldParticlesPacketWrapper;
import org.bukkit.Particle;
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
    private static final int PARTICLE_VIEW_DISTANCE = 64;

    private final JavaPlugin plugin;

    // Keeping track of the entities
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
        // Remove the arrow if it is no longer in the air
        if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround() || target.isDead() || !target.isValid()) {
            arrow.setGlowing(false);
            cancel();
            return;
        }

        guide();
        sendParticles();
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!event.getEntity().equals(arrow)) return;

        arrow.setGlowing(false);
        cancel();
    }

    @Override
    public void start() {
        arrow.setGravity(false);
        arrow.setGlowing(true);

        initialDistance = arrow.getLocation().distance(target.getLocation());
        runTaskTimer(plugin, 0, 1L);
    }

    /**
     * Computes the new guiding velocity for the arrow. The direction of the velocity depends on the ratio of its
     * current distance to the target to its initial distance. This means that the arrow initially flies in the
     * direction in which the player is shooting and aligns itself more and more towards its target the closer is gets
     * to the target.
     */
    private void guide() {
        double currentDistance = arrow.getLocation().distance(target.getLocation());
        double distanceRatio = currentDistance / initialDistance;

        Vector currentVelocity = arrow.getVelocity();
        double speed = currentVelocity.length();
        Vector targetDirection = target.getLocation().toVector().subtract(arrow.getLocation().toVector()).normalize();
        Vector newVelocity = currentVelocity.multiply(distanceRatio).add(targetDirection.multiply(1 - distanceRatio)).normalize().multiply(speed);

        arrow.setVelocity(newVelocity);
    }

    private void sendParticles() {
        WorldParticlesPacketWrapper packet = new WorldParticlesPacketWrapper(arrow.getLocation().toVector(), Particle.SOUL_FIRE_FLAME);
        packet.setSpeed(0);
        packet.setCount(5);
        packet.setOffset(new Vector3F(0f, 0f, 0f));
        packet.setLongDistance(true);
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet.getHandle(), arrow.getLocation(), PARTICLE_VIEW_DISTANCE);
    }
}
