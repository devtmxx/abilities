package de.tmxx.abilities.ability;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.Vector3F;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.inject.Inject;
import de.tmxx.abilities.wrapper.packet.WorldParticlesPacketWrapper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Project: abilities
 * 11.03.2025
 *
 * <p>
 *     This ability distributes the fall damage a player takes to surrounding entities. The distributed damage reduces
 *     the damage taken by the player himself.
 * </p>
 *
 * @author timmauersberger
 * @version 1.0
 */
public class FallDamageSpreadAbility implements Ability, Listener {
    // Radius within entities will take damage from the falling player
    private static final double MAX_DAMAGE_DISTANCE = 3.0D;

    // Positive and negative y direction to find blocks for the animation
    private static final int BLOCK_Y_BOUNDS = 2;

    // Radius within players should receive particle packets
    private static final int MAX_PARTICLE_OBSERVER_DISTANCE = 64;

    // Limit at which an explosion occurs
    private static final double EXPLOSION_FALL_DISTANCE = 10.0D;

    private final JavaPlugin plugin;

    @Inject
    FallDamageSpreadAbility(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register() {
        // Register this instance as an event listener in bukkit
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // Check if the damage is caused by falling
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        double baseDamage = event.getDamage();
        AtomicDouble damageDealt = new AtomicDouble(0);

        // Distribute fall damage to nearby entities
        distanceByEntitiesInReach(player).forEach((entity, distance) -> {
            double damage = calculateDamage(baseDamage, distance);
            entity.damage(damage, player);
            damageDealt.addAndGet(damage);
        });

        // Reduce the player's damage by the amount distributed
        event.setDamage(Math.max(0, baseDamage - damageDealt.get()));

        // Player animation based on fall distance
        playAnimation(player.getLocation(), player.getFallDistance() >= EXPLOSION_FALL_DISTANCE);
    }

    /**
     * Finds living entities within range and calculates their distance from the given player.
     */
    private Map<LivingEntity, Double> distanceByEntitiesInReach(Player player) {
        Map<LivingEntity, Double> entities = new HashMap<>();
        Location origin = player.getLocation();

        origin.getWorld().getNearbyLivingEntities(origin, MAX_DAMAGE_DISTANCE).forEach(entity -> {
            if (entity.equals(player)) return;

            double distance = entity.getLocation().distance(origin);
            entities.put(entity, distance);
        });

        return entities;
    }

    /**
     * Triggers visual effect at the player's location.
     */
    private void playAnimation(Location origin, boolean explosion) {
        Multimap<Integer, Block> blocks = blocksByDistance(origin);

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step >= blocks.keys().size()) {
                    cancel();
                    return;
                }

                blocks.get(step++).forEach(block -> {
                    playEffect(block);
                    if (explosion) playParticles(block.getLocation());
                });
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Groups blocks by their distance from the given origin.
     */
    private Multimap<Integer, Block> blocksByDistance(Location origin) {
        Multimap<Integer, Block> blocks = HashMultimap.create();
        int blockX = origin.getBlockX();
        int blockY = origin.getBlockY();
        int blockZ = origin.getBlockZ();
        int distance = (int) MAX_DAMAGE_DISTANCE;

        iterateWithBounds(blockX, distance, x -> iterateWithBounds(blockZ, distance, z -> {
            double distanceSquared = distanceSquared(x, blockX, z, blockZ);
            if (distanceSquared > distance * distance) return;

            Block block = highestBlockInBounds(origin.getWorld(), x, blockY, z);
            if (block == null) return;

            blocks.put((int) Math.sqrt(distanceSquared), block);
        }));

        return blocks;
    }

    /**
     * Calculates distributed damage based on distance.
     */
    private double calculateDamage(double baseDamage, double distance) {
        return (baseDamage / MAX_DAMAGE_DISTANCE) * (MAX_DAMAGE_DISTANCE - distance);
    }

    /**
     * Iterates over a range of coordinates within given bounds.
     */
    private void iterateWithBounds(int start, int bound, Consumer<Integer> coordinate) {
        for (int i = start - bound; i <= start + bound; i++) {
            coordinate.accept(i);
        }
    }

    /**
     * Computes squared distance between two points.
     */
    private double distanceSquared(int xa, int xb, int za, int zb) {
        return Math.pow(xa - xb, 2) + Math.pow(za - zb, 2);
    }

    /**
     * Finds the highest solid block within a vertical range.
     */
    private Block highestBlockInBounds(World world, int x, int y, int z) {
        for (int i = y + BLOCK_Y_BOUNDS; i >= y - BLOCK_Y_BOUNDS; i--) {
            Block block = world.getBlockAt(x, i ,z);
            if (block.isSolid()) return block;
        }
        return null;
    }

    /**
     * Triggers a block effect.
     */
    private void playEffect(Block block) {
        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getBlockData());
    }

    /**
     * Spawns explosion particles at the given location.
     */
    private void playParticles(Location origin) {
        WorldParticlesPacketWrapper packet = new WorldParticlesPacketWrapper(origin.toVector(), Particle.EXPLOSION);
        packet.setCount(1);
        packet.setOffset(new Vector3F(0, 0, 0));
        packet.setSpeed(0);
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(
                packet.getHandle(),
                origin,
                MAX_PARTICLE_OBSERVER_DISTANCE
        );
    }
}
