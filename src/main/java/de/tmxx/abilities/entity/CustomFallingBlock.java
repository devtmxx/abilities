package de.tmxx.abilities.entity;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import de.tmxx.abilities.util.PacketBroadcaster;
import de.tmxx.abilities.wrapper.PositionMoveRotationWrapper;
import de.tmxx.abilities.wrapper.Vec3Wrapper;
import de.tmxx.abilities.wrapper.packet.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class CustomFallingBlock implements CustomEntity {
    private final int entityId;
    private final UUID uniqueId = UUID.randomUUID();

    private boolean spawned = false;

    private Location location = null;
    private FallingBlockType type = FallingBlockType.BLUE_ICE;
    private boolean noGravity = true;

    public CustomFallingBlock() {
        entityId = generateEntityId();
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public World getWorld() {
        return location == null ? null : location.getWorld();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setNoGravity(boolean noGravity) {
        this.noGravity = noGravity;

        if (spawned) updateMetadata();
    }

    @Override
    public boolean isNoGravity() {
        return noGravity;
    }

    @Override
    public void move(Location to) {
        Bukkit.broadcast(Component.text("FROM: " + location.toString()));
        Bukkit.broadcast(Component.text("TO: " + to.toString()));
        if (!spawned) {
            location = to;
            return;
        }

        if (!location.getWorld().equals(to.getWorld())) {
            teleport(to);
            return;
        }

        double distanceSquared = location.distanceSquared(to);
        if (distanceSquared >= MAX_MOVE_DISTANCE_SQUARED) {
            teleport(to);
            return;
        }

        RelEntityMoveLookPacketWrapper packet = new RelEntityMoveLookPacketWrapper(entityId);
        packet.setDeltaX(to.getX() - location.getX());
        packet.setDeltaY(to.getY() - location.getY());
        packet.setDeltaZ(to.getZ() - location.getZ());
        packet.setYaw(to.getYaw());
        packet.setPitch(to.getPitch());
        packet.setOnGround(false);
        broadcastPacket(packet);

        location = to;
    }

    @Override
    public void teleport(Location to) {
        if (!spawned) {
            location = to;
            return;
        }

        if (!location.getWorld().equals(to.getWorld())) {
            remove();
            spawn(to);
            return;
        }

        location = to;

        EntityTeleportPacketWrapper packet = new EntityTeleportPacketWrapper(entityId, new PositionMoveRotationWrapper(
                new Vec3Wrapper(
                        location.getX(),
                        location.getY(),
                        location.getZ()
                ),
                location.getYaw(),
                location.getPitch()
        ));
        packet.setOnGround(false);
        broadcastPacket(packet);
    }

    @Override
    public void spawn(@NotNull Location location) {
        if (spawned) return;
        spawned = true;

        this.location = location;
        SpawnEntityPacketWrapper packet = new SpawnEntityPacketWrapper(entityId, EntityType.FALLING_BLOCK, location);
        packet.setUniqueId(uniqueId);
        packet.setData(type.getProtocolId());

        broadcastPacket(packet);

        updateMetadata();
    }

    @Override
    public void remove() {
        if (!spawned) return;
        spawned = false;

        broadcastPacket(new EntityDestroyPacketWrapper(entityId));
    }

    public void setType(FallingBlockType type) {
        this.type = type;

        if (spawned) {
            // cannot change the type of falling block, so instead remove the old one and replace it with a new entity
            // with the correct type
            remove();
            spawn(location);
        }
    }

    private void updateMetadata() {
        EntityMetadataPacketWrapper packet = new EntityMetadataPacketWrapper(entityId);
        packet.setMetadata(EntityMetadataIndex.NO_GRAVITY, noGravity);

        broadcastPacket(packet);
    }

    private void broadcastPacket(PacketWrapper packet) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        Bukkit.getOnlinePlayers().stream().filter(this::isVisibleTo).forEach(player -> {
            manager.sendServerPacket(player, packet.getHandle());
        });
    }
}
