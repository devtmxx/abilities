package de.tmxx.abilities.wrapper.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import java.util.List;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class EntityDestroyPacketWrapper implements PacketWrapper {
    private static final PacketType PACKET_TYPE = PacketType.Play.Server.ENTITY_DESTROY;
    private static final int ENTITY_IDS = 0;

    private final PacketContainer handle;

    public EntityDestroyPacketWrapper() {
        handle = ProtocolLibrary.getProtocolManager().createPacket(PACKET_TYPE);
    }

    public EntityDestroyPacketWrapper(List<Integer> entityIds) {
        this();

        setEntityIds(entityIds);
    }

    public void setEntityIds(List<Integer> entityIds) {
        handle.getIntLists().write(ENTITY_IDS, entityIds);
    }

    @Override
    public PacketContainer getHandle() {
        return handle;
    }
}
