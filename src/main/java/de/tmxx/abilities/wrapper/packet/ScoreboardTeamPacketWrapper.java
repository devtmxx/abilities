package de.tmxx.abilities.wrapper.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * Project: abilities
 * 14.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class ScoreboardTeamPacketWrapper implements PacketWrapper {
    private static final PacketType PACKET_TYPE = PacketType.Play.Server.SCOREBOARD_TEAM;
    private static final int METHOD = 0;
    private static final int NAME = 1;
    private static final int PLAYERS = 2;
    private static final int PARAMETERS = 3;

    private final PacketContainer handle;

    public ScoreboardTeamPacketWrapper() {
        handle = ProtocolLibrary.getProtocolManager().createPacket(PACKET_TYPE);
    }

    public void setMethod(int method) {
        handle.getModifier().write(METHOD, method);
    }

    public void setName(String name) {
        handle.getModifier().write(NAME, name);
    }

    public void setPlayers(Collection<String> players) {
        handle.getModifier().write(PLAYERS, players);
    }

    public void setParameters(@Nullable Object parameters) {
        handle.getModifier().write(PARAMETERS, Optional.ofNullable(parameters));
    }

    @Override
    public PacketContainer getHandle() {
        return handle;
    }

    public static final class Method {
        public static final int ADD = 0;
        public static final int REMOVE = 1;
        public static final int CHANGE = 2;
        public static final int JOIN = 3;
        public static final int LEAVE = 4;
    }
}
