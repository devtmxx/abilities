package de.tmxx.abilities;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedParticle;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Project: abilities
 * 07.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class EnderShotAbility implements Listener {
    private final Plugin plugin;

    public EnderShotAbility(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (!player.getInventory().getItemInMainHand().getType().equals(Material.CROSSBOW)) return;

        event.getEntity().remove();
        shoot(player);
    }

    private void shoot(Player player) {
        double speed = 1D;

        Location center = player.getEyeLocation().clone().subtract(0, 0.8, 0);
        Vector direction = player.getEyeLocation().getDirection().clone().normalize().multiply(speed);

        ArmorStand armorStand = spawn(center);

        new BukkitRunnable() {
            final float pitch = center.getPitch() + 90;
            int steps = 0;
            final double xAngle = pitchToAngle(pitch);
            double animatedAngle = 0;
            final double angleSteps = Math.PI / 6;

            @Override
            public void run() {
                if (steps == 200) {
                    armorStand.remove();
                    cancel();
                    return;
                }
                steps++;

                animatedAngle += angleSteps;
                if (animatedAngle >= Math.PI * 2) animatedAngle -= Math.PI * 2;

                if (armorStand.isDead()) {
                    cancel();
                    return;
                }

                EulerAngle angle = new EulerAngle(xAngle, animatedAngle * angleYMultiplier(pitch), animatedAngle * angleZMultiplier(pitch));

                armorStand.setHeadPose(angle);
                sendParticle(armorStand.getLocation().clone().add(0, 0.75, 0).subtract(direction.clone().normalize().multiply(speed * 3)));
                armorStand.getWorld().playSound(armorStand.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 2f);
                armorStand.setVelocity(direction.clone());

                AtomicBoolean remove = new AtomicBoolean(false);

                armorStand.getWorld().getNearbyLivingEntities(armorStand.getLocation(), 0.5).stream().filter(e -> !e.equals(player) && !e.equals(armorStand)).forEach(entity -> {
                    entity.damage(10D, player);
                    remove.set(true);
                });

                if (armorStand.getLocation().clone().add(direction.clone().normalize().multiply(0.2)).getBlock().isSolid()) {
                    remove.set(true);
                }

                if (remove.get()) {
                    center.getWorld().createExplosion(armorStand.getLocation(), 1, true, true, player);
                    armorStand.remove();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private ArmorStand spawn(Location location) {
        ArmorStand armorStand = location.getWorld().spawn(location.clone().add(0, 100, 0), ArmorStand.class);
        armorStand.setInvisible(true);
        armorStand.setBasePlate(false);
        armorStand.setGravity(true);
        armorStand.setSmall(true);
        armorStand.setItem(EquipmentSlot.HEAD, ItemStack.of(Material.DRAGON_EGG));
        armorStand.setHeadPose(new EulerAngle(pitchToAngle(location.getPitch() + 90), 0, 0));
        armorStand.teleport(location);
        return armorStand;
    }

    private void sendParticle(Location location) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.WORLD_PARTICLES);

        packet.getBooleans().write(0, true);
        packet.getBooleans().write(1, true);
        packet.getNewParticles().write(0, WrappedParticle.create(Particle.CAMPFIRE_COSY_SMOKE, null));
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());
        packet.getFloat().write(0, 0.2f); // Offset X
        packet.getFloat().write(1, 0.2f); // Offset Y
        packet.getFloat().write(2, 0.2f); // Offset Z
        packet.getFloat().write(3, 0f); // Speed
        packet.getIntegers().write(0, 5); // Particle count

        protocolManager.broadcastServerPacket(packet);
    }

    private double pitchToAngle(float pitch) {
        return pitch < 45 ? 0 : pitch <= 135 ? Math.PI / 2 : Math.PI;
    }

    private int angleYMultiplier(float pitch) {
        return pitch >= 45 && pitch <= 135 ? 0 : 1;
    }

    private int angleZMultiplier(float pitch) {
        return pitch >= 45 && pitch <= 135 ? 1 : 0;
    }
}
