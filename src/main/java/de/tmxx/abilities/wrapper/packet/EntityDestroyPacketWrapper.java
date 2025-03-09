package de.tmxx.abilities.wrapper.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class EntityDestroyPacketWrapper implements PacketWrapper {
    private static final PacketType PACKET_TYPE = PacketType.Play.Server.ENTITY_METADATA;
    private static final int ENTITY_IDS_INDEX = 0;

    private final PacketContainer handle;

    public EntityDestroyPacketWrapper() {
        handle = ProtocolLibrary.getProtocolManager().createPacket(PACKET_TYPE);
    }

    public EntityDestroyPacketWrapper(int... entityIds) {
        this();

        setEntityIds(entityIds);
    }

    public void setEntityIds(int[] entityIds) {
        handle.getIntegerArrays().write(ENTITY_IDS_INDEX, entityIds);
    }

    @Override
    public PacketContainer getHandle() {
        return handle;
    }
}
