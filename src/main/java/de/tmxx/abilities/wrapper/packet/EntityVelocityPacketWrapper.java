package de.tmxx.abilities.wrapper.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import de.tmxx.abilities.util.PacketBroadcaster;
import de.tmxx.abilities.util.ProtocolUtils;
import org.bukkit.util.Vector;

/**
 * Project: abilities
 * 11.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class EntityVelocityPacketWrapper implements PacketWrapper {
    private static final PacketType PACKET_TYPE = PacketType.Play.Server.ENTITY_VELOCITY;
    private static final int ENTITY_ID = 0;
    private static final int VELOCITY_X = 1;
    private static final int VELOCITY_Y = 2;
    private static final int VELOCITY_Z = 3;

    private final PacketContainer handle;

    public EntityVelocityPacketWrapper() {
        handle = ProtocolLibrary.getProtocolManager().createPacket(PACKET_TYPE);
    }

    public EntityVelocityPacketWrapper(int entityId) {
        this();

        setEntityId(entityId);
    }

    public EntityVelocityPacketWrapper(int entityId, Vector velocity) {
        this(entityId);

        setVelocity(velocity);
    }

    public EntityVelocityPacketWrapper(int entityId, float velocityX, float velocityY, float velocityZ) {
        this(entityId, new Vector(velocityX, velocityY, velocityZ));
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(ENTITY_ID, entityId);
    }

    public void setVelocity(Vector velocity) {
        setVelocityX(velocity.getX());
        setVelocityY(velocity.getY());
        setVelocityZ(velocity.getZ());
    }

    public void setVelocityX(double velocityX) {
        handle.getIntegers().write(VELOCITY_X, (int) ProtocolUtils.toVelocity(velocityX));
    }

    public void setVelocityY(double velocityY) {
        handle.getIntegers().write(VELOCITY_Y, (int) ProtocolUtils.toVelocity(velocityY));
    }

    public void setVelocityZ(double velocityZ) {
        handle.getIntegers().write(VELOCITY_Z, (int) ProtocolUtils.toVelocity(velocityZ));
    }

    @Override
    public PacketContainer getHandle() {
        return handle;
    }
}
