package de.tmxx.abilities.wrapper.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import de.tmxx.abilities.util.ProtocolUtils;
import org.bukkit.util.Vector;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class RelEntityMoveLookPacketWrapper implements PacketWrapper {
    private static final PacketType PACKET_TYPE = PacketType.Play.Server.REL_ENTITY_MOVE_LOOK;

    private static final int ENTITY_ID = 0;
    private static final int DELTA_X = 0;
    private static final int DELTA_Y = 1;
    private static final int DELTA_Z = 2;
    private static final int YAW = 0;
    private static final int PITCH = 1;
    private static final int ON_GROUND = 0;
    private static final int HAS_ROT = 1;
    private static final int HAS_POS = 2;

    private final PacketContainer handle;

    public RelEntityMoveLookPacketWrapper() {
        handle = ProtocolLibrary.getProtocolManager().createPacket(PACKET_TYPE);
    }

    public RelEntityMoveLookPacketWrapper(int entityId) {
        this();

        setEntityId(entityId);
    }

    public RelEntityMoveLookPacketWrapper(int entityId, Vector movement) {
        this(entityId);

        setDeltaX(movement.getX());
        setDeltaY(movement.getY());
        setDeltaZ(movement.getZ());
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(ENTITY_ID, entityId);
    }

    public void setDeltaX(double deltaX) {
        handle.getShorts().write(DELTA_X, ProtocolUtils.toMovement(deltaX));
        hasPos();
    }

    public void setDeltaY(double deltaY) {
        handle.getShorts().write(DELTA_Y, ProtocolUtils.toMovement(deltaY));
        hasPos();
    }

    public void setDeltaZ(double deltaZ) {
        handle.getShorts().write(DELTA_Z, ProtocolUtils.toMovement(deltaZ));
        hasPos();
    }

    public void setYaw(float yaw) {
        handle.getBytes().write(YAW, ProtocolUtils.toAngle(yaw));
        hasRot();
    }

    public void setPitch(float pitch) {
        handle.getBytes().write(PITCH, ProtocolUtils.toAngle(pitch));
        hasRot();
    }

    public void setOnGround(boolean onGround) {
        handle.getBooleans().write(ON_GROUND, onGround);
    }

    public void hasPos() {
        handle.getBooleans().write(HAS_POS, true);
    }

    public void hasRot() {
       handle.getBooleans().write(HAS_ROT, true);
    }

    @Override
    public PacketContainer getHandle() {
        return handle;
    }
}
