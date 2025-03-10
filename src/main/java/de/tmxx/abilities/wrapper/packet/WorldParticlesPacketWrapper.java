package de.tmxx.abilities.wrapper.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedParticle;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Project: abilities
 * 10.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class WorldParticlesPacketWrapper implements PacketWrapper {
    private static final PacketType PACKET_TYPE = PacketType.Play.Server.WORLD_PARTICLES;

    private static final int LONG_DISTANCE = 0;
    private static final int ALWAYS_VISIBLE = 1;
    private static final int PARTICLE_TYPE = 0;
    private static final int POSITION_X = 0;
    private static final int POSITION_Y = 1;
    private static final int POSITION_Z = 2;
    private static final int OFFSET_X = 0;
    private static final int OFFSET_Y = 1;
    private static final int OFFSET_Z = 2;
    private static final int SPEED = 3;
    private static final int PARTICLE_COUNT = 0;

    private final PacketContainer handle;

    public WorldParticlesPacketWrapper() {
        handle = ProtocolLibrary.getProtocolManager().createPacket(PACKET_TYPE);
    }

    public WorldParticlesPacketWrapper(Vector position, Particle particle) {
        this();

        setPosition(position);
        setParticle(particle);
    }

    public WorldParticlesPacketWrapper(Vector position, Particle particle, int count) {
        this(position, particle);

        setCount(count);
    }

    public void setLongDistance(boolean longDistance) {
        handle.getBooleans().write(LONG_DISTANCE, longDistance);
    }

    public void setAlwaysVisible(boolean alwaysVisible) {
        handle.getBooleans().write(ALWAYS_VISIBLE, alwaysVisible);
    }

    public void setParticle(Particle particle) {
        setParticle(particle, null);
    }

    public void setParticle(Particle particle, Object data) {
        WrappedParticle<?> wrappedParticle = WrappedParticle.create(particle, data);
        handle.getNewParticles().write(PARTICLE_TYPE, wrappedParticle);
    }

    public void setPositionX(double positionX) {
        handle.getDoubles().write(POSITION_X, positionX);
    }

    public void setPositionY(double positionY) {
        handle.getDoubles().write(POSITION_Y, positionY);
    }

    public void setPositionZ(double positionZ) {
        handle.getDoubles().write(POSITION_Z, positionZ);
    }

    public void setPosition(Vector position) {
        setPositionX(position.getX());
        setPositionY(position.getY());
        setPositionZ(position.getZ());
    }

    public void setOffsetX(float offsetX) {
        handle.getFloat().write(OFFSET_X, offsetX);
    }

    public void setOffsetY(float offsetY) {
        handle.getFloat().write(OFFSET_Y, offsetY);
    }

    public void setOffsetZ(float offsetZ) {
        handle.getFloat().write(OFFSET_Z, offsetZ);
    }

    public void setOffset(Vector3F offset) {
        setOffsetX(offset.getX());
        setOffsetY(offset.getY());
        setOffsetZ(offset.getZ());
    }

    public void setSpeed(float speed) {
        handle.getFloat().write(SPEED, speed);
    }

    public void setCount(int count) {
        handle.getIntegers().write(PARTICLE_COUNT, count);
    }

    @Override
    public PacketContainer getHandle() {
        return handle;
    }
}
