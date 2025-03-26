package de.tmxx.abilities.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface CustomEntity {
    int MIN_ID = 5000;
    AtomicInteger CURRENT_ID = new AtomicInteger(MIN_ID);

    int VIEW_DISTANCE_BLOCKS_SQUARED = 4096; // 64 blocks
    int VIEW_DISTANCE = 64; // 64 blocks
    int MAX_MOVE_DISTANCE_SQUARED = 64; // 8 blocks

    /**
     * Generates a new entity id.
     */
    default int generateEntityId() {
        return CURRENT_ID.getAndIncrement();
    }

    /**
     * Check if an entity is visible to a player, i.e. is in his view distance.
     */
    default boolean isVisibleTo(Player player) {
        if (getWorld() == null) return false;
        if (!getWorld().equals(player.getWorld())) return false;
        return getLocation().distanceSquared(player.getLocation()) <= VIEW_DISTANCE_BLOCKS_SQUARED;
    }

    int getEntityId();
    UUID getUniqueId();
    World getWorld();
    Location getLocation();
    void setNoGravity(boolean noGravity);
    boolean isNoGravity();
    void move(Location to);
    void teleport(Location to);
    void setVelocity(Vector velocity);
    void spawn(@NotNull Location location);
    void remove();
}
