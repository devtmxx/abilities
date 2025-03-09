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
    private static final PacketType PACKET_TYPE = PacketType.Play.Server.REL_ENTITY_MOVE;

    private static final int ENTITY_ID_INDEX = 0;
    private static final int DELTA_X_INDEX = 0;
    private static final int DELTA_Y_INDEX = 1;
    private static final int DELTA_Z_INDEX = 2;
    private static final int YAW_INDEX = 0;
    private static final int PITCH_INDEX = 1;
    private static final int ON_GROUND_INDEX = 0;
    private static final int HAS_ROT_INDEX = 1;
    private static final int HAS_POS_INDEX = 2;

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
        handle.getIntegers().write(ENTITY_ID_INDEX, entityId);
    }

    public void setDeltaX(double deltaX) {
        handle.getShorts().write(DELTA_X_INDEX, ProtocolUtils.toMovement(deltaX));
        hasPos();
    }

    public void setDeltaY(double deltaY) {
        handle.getShorts().write(DELTA_Y_INDEX, ProtocolUtils.toMovement(deltaY));
        hasPos();
    }

    public void setDeltaZ(double deltaZ) {
        handle.getShorts().write(DELTA_Z_INDEX, ProtocolUtils.toMovement(deltaZ));
        hasPos();
    }

    public void setYaw(float yaw) {
        //handle.getBytes().write(YAW_INDEX, ProtocolUtils.toAngle(yaw));
        hasRot();
    }

    public void setPitch(float pitch) {
        //handle.getBytes().write(PITCH_INDEX, ProtocolUtils.toAngle(pitch));
        hasRot();
    }

    public void setOnGround(boolean onGround) {
        handle.getBooleans().write(ON_GROUND_INDEX, onGround);
    }

    public void hasPos() {
        //handle.getBooleans().write(HAS_POS_INDEX, true);
    }

    public void hasRot() {
       //handle.getBooleans().write(HAS_ROT_INDEX, true);
    }

    @Override
    public PacketContainer getHandle() {
        return handle;
    }
}
