package de.tmxx.abilities.wrapper;

import com.comphenix.protocol.utility.MinecraftReflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class PositionMoveRotationWrapper {
    final static Class<?> MINECRAFT_CLASS;
    private static Constructor<?> constructor;

    private final Object handle;

    public PositionMoveRotationWrapper() {
        this(Vec3Wrapper.zero());
    }

    public PositionMoveRotationWrapper(Vec3Wrapper position) {
        this(position, 0, 0);
    }

    public PositionMoveRotationWrapper(Vec3Wrapper position, float yaw, float pitch) {
        this(position, Vec3Wrapper.zero(), yaw, pitch);
    }

    public PositionMoveRotationWrapper(Vec3Wrapper position, Vec3Wrapper velocity) {
        this(position, velocity, 0, 0);
    }

    public PositionMoveRotationWrapper(Vec3Wrapper position, Vec3Wrapper velocity, float yaw, float pitch) {
        try {
            handle = getConstructor().newInstance(position.getHandle(), velocity.getHandle(), yaw, pitch);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getHandle() {
        return handle;
    }

    private Constructor<?> getConstructor() {
        if (constructor == null) {
            try {
                constructor = MINECRAFT_CLASS.getConstructor(
                        Vec3Wrapper.MINECRAFT_CLASS,
                        Vec3Wrapper.MINECRAFT_CLASS,
                        float.class,
                        float.class
                );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return constructor;
    }

    static {
        MINECRAFT_CLASS = MinecraftReflection.getMinecraftClass("world.entity.PositionMoveRotation");
    }
}
