package de.tmxx.abilities.wrapper.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import de.tmxx.abilities.util.ProtocolUtils;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class SpawnEntityPacketWrapper implements PacketWrapper {
    private static final PacketType PACKET_TYPE = PacketType.Play.Server.SPAWN_ENTITY;

    private static final int ENTITY_ID_INDEX = 0;
    private static final int UNIQUE_ID_INDEX = 0;
    private static final int ENTITY_TYPE_INDEX = 0;

    private static final int POSITION_X_INDEX = 0;
    private static final int POSITION_Y_INDEX = 1;
    private static final int POSITION_Z_INDEX = 2;

    private static final int PITCH_INDEX = 1;
    private static final int BODY_YAW_INDEX = 2;
    private static final int HEAD_YAW_INDEX = 3;

    private static final int DATA_INDEX = 4;

    private static final int VELOCITY_X_INDEX = 0;
    private static final int VELOCITY_Y_INDEX = 1;
    private static final int VELOCITY_Z_INDEX = 2;

    private final PacketContainer handle;

    public SpawnEntityPacketWrapper() {
        handle = ProtocolLibrary.getProtocolManager().createPacket(PACKET_TYPE);

        setUniqueId(UUID.randomUUID());
    }

    public SpawnEntityPacketWrapper(int entityId, EntityType entityType) {
        this();

        setEntityId(entityId);
        setEntityType(entityType);
    }

    public SpawnEntityPacketWrapper(int entityId, EntityType entityType, Location location) {
        this(entityId, entityType);

        setLocation(location);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(ENTITY_ID_INDEX, entityId);
    }

    public void setEntityType(EntityType entityType) {
        handle.getEntityTypeModifier().write(ENTITY_TYPE_INDEX, entityType);
    }

    public void setUniqueId(UUID uniqueId) {
        handle.getUUIDs().write(UNIQUE_ID_INDEX, uniqueId);
    }

    public void setLocation(Location location) {
        handle.getDoubles().write(POSITION_X_INDEX, location.getX());
        handle.getDoubles().write(POSITION_Y_INDEX, location.getY());
        handle.getDoubles().write(POSITION_Z_INDEX, location.getZ());

        handle.getIntegers().write(HEAD_YAW_INDEX, (int) ProtocolUtils.toAngle(location.getYaw()));
        handle.getIntegers().write(BODY_YAW_INDEX, (int) ProtocolUtils.toAngle(location.getYaw()));
        handle.getIntegers().write(PITCH_INDEX, (int) ProtocolUtils.toAngle(location.getPitch()));
    }

    public void setData(int data) {
        handle.getIntegers().write(DATA_INDEX, data);
    }

    public void setVelocity(Vector velocity) {
        handle.getShorts().write(VELOCITY_X_INDEX, ProtocolUtils.toVelocity(velocity.getX()));
        handle.getShorts().write(VELOCITY_Y_INDEX, ProtocolUtils.toVelocity(velocity.getY()));
        handle.getShorts().write(VELOCITY_Z_INDEX, ProtocolUtils.toVelocity(velocity.getZ()));
    }

    @Override
    public PacketContainer getHandle() {
        return handle;
    }
}
