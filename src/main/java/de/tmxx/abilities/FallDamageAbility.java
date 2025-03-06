package de.tmxx.abilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Project: abilities
 * 06.03.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class FallDamageAbility implements Listener {
    private static final double MAX_DAMAGE_DISTANCE = 3;

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

        playAnimation(player.getLocation());
    }

    private double calculateDamage(double baseDamage, double distance) {
        return (baseDamage / MAX_DAMAGE_DISTANCE) * distance;
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

    public static void playAnimation(Location location) {
        List<Block> blocks = new ArrayList<>();
        for (int x = location.getBlockX() - (int) MAX_DAMAGE_DISTANCE; x <= location.getBlockX() + MAX_DAMAGE_DISTANCE; x++) {
            for (int z = location.getBlockZ() - (int) MAX_DAMAGE_DISTANCE; z <= location.getBlockZ() + MAX_DAMAGE_DISTANCE; z++) {
                int startY = location.getBlockY() - 2;
                int endY = location.getBlockY() + 2;
                Block block = location.getWorld().getBlockAt(x, startY, z);
                while (block.getY() < endY) {
                    Block relative = block.getRelative(BlockFace.UP);
                    if (relative.isEmpty()) break;

                    block = relative;
                }

                if (block.isEmpty() || block.isPassable()) continue;
                if (block.getLocation().distanceSquared(location) > Math.pow(MAX_DAMAGE_DISTANCE, 2)) continue;

                blocks.add(block);
            }
        }

        for (Block block : blocks) {
            /*ArmorStand armorStand = block.getWorld().spawn(block.getLocation(), ArmorStand.class);
            armorStand.setItem(EquipmentSlot.HEAD, ItemStack.of(block.getType()));
            armorStand.setInvisible(true);
            armorStand.setBasePlate(false);
            /*FallingBlock fallingBlock = block.getWorld().spawn(block.getLocation(), FallingBlock.class);
            fallingBlock.setBlockData(block.getBlockData());
            fallingBlock.setBlockState(block.getState());
            fallingBlock.setVelocity(new Vector(0, 1, 0));
            fallingBlock.setDropItem(false);
            fallingBlock.setHurtEntities(false);
            block.setType(Material.AIR);*/

            spawnFallingBlock(block);
        }
    }

    private static void spawnFallingBlock(Block block) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        packet.getModifier().getFields().forEach(field -> {
            Bukkit.broadcastMessage(field.getField().getName() + ": " + field.getField().getType().getName());
        });

        packet.getIntegers().write(0, 9999);
        packet.getUUIDs().write(0, UUID.randomUUID());
        packet.getEntityTypeModifier().write(0, EntityType.FALLING_BLOCK);

        packet.getDoubles().write(0, (double) block.getX());
        packet.getDoubles().write(1, (double) block.getY() + 1D);
        packet.getDoubles().write(2, (double) block.getZ());

        packet.getIntegers().write(1, 1);

        packet.getIntegers().write(2, convertVelocity(0));
        packet.getIntegers().write(3, convertVelocity(4));
        packet.getIntegers().write(4, convertVelocity(0));

        manager.broadcastServerPacket(packet);
    }

    private static int convertVelocity(double velocity) {
    /*
      Minecraft represents a velocity within 4 blocks per second, in any direction,
      by using the entire Short range, meaning you can only move up to 4 blocks/second
      on any given direction
    */
        return (int) (clamp(velocity, -3.9, 3.9) * 8000);
    }

    private static double clamp(double targetNum, double min, double max) {
        // Makes sure a number is within a range
        return Math.max(min, Math.min(targetNum, max));
    }
}
