package de.tmxx.abilities.wrapper.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class EntityMetadataPacketWrapper implements PacketWrapper {
    private static final PacketType PACKET_TYPE = PacketType.Play.Server.ENTITY_METADATA;

    private static final int ENTITY_ID_INDEX = 0;
    private static final int METADATA_INDEX = 0;

    private final PacketContainer handle;
    private final Map<Integer, Object> metadata = new HashMap<>();

    public EntityMetadataPacketWrapper(int entityId) {
        handle = ProtocolLibrary.getProtocolManager().createPacket(PACKET_TYPE);

        setEntityId(entityId);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(ENTITY_ID_INDEX, entityId);
    }

    public void setMetadata(int index, Object value) {
        metadata.put(index, value);
    }

    @Override
    public PacketContainer getHandle() {
        List<WrappedDataValue> metadata = compileMetadata();
        handle.getDataValueCollectionModifier().write(METADATA_INDEX, metadata);
        return handle;
    }

    private List<WrappedDataValue> compileMetadata() {
        List<WrappedDataValue> list = new ArrayList<>();
        metadata.forEach((index, value) -> {
            list.add(new WrappedDataValue(index, WrappedDataWatcher.Registry.get(value.getClass()), value));
        });
        return list;
    }
}
