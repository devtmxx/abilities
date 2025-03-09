package de.tmxx.abilities.wrapper.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import de.tmxx.abilities.wrapper.PositionMoveRotationWrapper;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class EntityTeleportPacketWrapper implements PacketWrapper {
    private static final PacketType PACKET_TYPE = PacketType.Play.Server.ENTITY_TELEPORT;

    private static final int ENTITY_ID_INDEX = 0;
    private static final int POSITION_MOVE_ROTATION_INDEX = 1;
    private static final int ON_GROUND_INDEX = 0;

    private final PacketContainer handle;

    public EntityTeleportPacketWrapper() {
        handle = ProtocolLibrary.getProtocolManager().createPacket(PACKET_TYPE);
    }

    public EntityTeleportPacketWrapper(int entityId) {
        this();

        setEntityId(entityId);
    }

    public EntityTeleportPacketWrapper(int entityId, PositionMoveRotationWrapper posMovRot) {
        this(entityId);

        setPosMovRot(posMovRot);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(ENTITY_ID_INDEX, entityId);
    }

    public void setPosMovRot(PositionMoveRotationWrapper posMovRot) {
        handle.getModifier().write(POSITION_MOVE_ROTATION_INDEX, posMovRot.getHandle());
    }

    public void setOnGround(boolean onGround) {
        handle.getBooleans().write(ON_GROUND_INDEX, onGround);
    }

    @Override
    public PacketContainer getHandle() {
        return handle;
    }
}
