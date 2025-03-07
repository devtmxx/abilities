package de.tmxx.abilities;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedParticle;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Project: abilities
 * 06.03.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class FallDamageAbility implements Listener {
    private static final double MAX_DAMAGE_DISTANCE = 3;

    private final Plugin plugin;

    public FallDamageAbility(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        double baseDamage = event.getDamage();
        Map<LivingEntity, Double> surroundingPlayers = getSurroundingPlayers(player);

        AtomicDouble damageDealt = new AtomicDouble(0);
        surroundingPlayers.forEach((entity, distance) -> {
            double damage = calculateDamage(baseDamage, distance);
            entity.damage(damage, player);
            damageDealt.addAndGet(damage);
        });

        event.setDamage(Math.max(0, baseDamage - damageDealt.get()));

        playAnimation(player.getLocation(), plugin);
    }

    private double calculateDamage(double baseDamage, double distance) {
        return (baseDamage / MAX_DAMAGE_DISTANCE) * (MAX_DAMAGE_DISTANCE - distance);
    }

    private Map<LivingEntity, Double> getSurroundingPlayers(Player player) {
        Map<LivingEntity, Double> surroundingPlayers = new HashMap<>();
        player.getLocation().getWorld().getNearbyEntities(player.getLocation(), MAX_DAMAGE_DISTANCE, MAX_DAMAGE_DISTANCE, MAX_DAMAGE_DISTANCE).forEach(entity -> {
            if (!(entity instanceof LivingEntity livingEntity)) return;
            if (livingEntity.equals(player)) return;

            double distance = livingEntity.getLocation().distance(player.getLocation());
            if (distance > MAX_DAMAGE_DISTANCE) return;

            surroundingPlayers.put(livingEntity, distance);
        });
        return surroundingPlayers;
    }

    public static void playAnimation(Location location, Plugin plugin) {
        Multimap<Integer, Block> blocks = HashMultimap.create();
        for (int x = location.getBlockX() - (int) MAX_DAMAGE_DISTANCE; x <= location.getBlockX() + MAX_DAMAGE_DISTANCE; x++) {
            for (int z = location.getBlockZ() - (int) MAX_DAMAGE_DISTANCE; z <= location.getBlockZ() + MAX_DAMAGE_DISTANCE; z++) {
                double distanceSquared = Math.pow(x - location.getBlockX(), 2) + Math.pow(z - location.getBlockZ(), 2);
                if (distanceSquared > MAX_DAMAGE_DISTANCE * MAX_DAMAGE_DISTANCE) continue;

                Block block = highestBlockInBounds(location.getWorld(), x, z, location.getBlockY() - 2, location.getBlockY() + 1);

                blocks.put((int) Math.sqrt(distanceSquared), block);
            }
        }

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (step >= blocks.keys().size()) {
                    cancel();
                    return;
                }

                blocks.get(step++).forEach(block -> spawnFallingBlock(block, plugin));
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private static Block highestBlockInBounds(World world, int x, int z, int minY, int maxY) {
        for (int y = maxY; y >= minY; y--) {
            Block block = world.getBlockAt(x, y, z);
            if (block.isSolid()) return block;
        }

        return null;
    }

    private static void spawnFallingBlock(Block block, Plugin plugin) {
        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getBlockData());
        sendParticle(block.getLocation().clone().add(0.5, 0.5, 0.5));
    }

    private static void sendParticle(Location location) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.WORLD_PARTICLES);

        packet.getBooleans().write(0, false);
        packet.getBooleans().write(1, true);
        packet.getNewParticles().write(0, WrappedParticle.create(Particle.EXPLOSION, null));
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());
        packet.getFloat().write(0, 0f); // Offset X
        packet.getFloat().write(1, 0f); // Offset Y
        packet.getFloat().write(2, 0f); // Offset Z
        packet.getFloat().write(3, 0f); // Speed
        packet.getIntegers().write(0, 1); // Particle count

        protocolManager.broadcastServerPacket(packet);
    }
}
