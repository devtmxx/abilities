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
public class Vec3Wrapper {
    static final Class<?> MINECRAFT_CLASS;
    private static Constructor<?> constructor;

    private double x;
    private double y;
    private double z;

    private Object handle;

    public Vec3Wrapper(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        try {
            handle = getConstructor().newInstance(x, y, z);
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
                constructor = MINECRAFT_CLASS.getConstructor(double.class, double.class, double.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return constructor;
    }

    static Vec3Wrapper zero() {
        return new Vec3Wrapper(0, 0, 0);
    }

    static {
        MINECRAFT_CLASS = MinecraftReflection.getMinecraftClass("world.phys.Vec3");
    }
}
