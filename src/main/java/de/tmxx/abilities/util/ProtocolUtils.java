package de.tmxx.abilities.util;

/**
 * Project: abilities
 * 09.03.2025
 *
 * <p>
 *     Converts values to minecraft protocol ready values.
 * </p>
 *
 * @author timmauersberger
 * @version 1.0
 */
public class ProtocolUtils {
    private static final double MAX_VELOCITY = 4;
    private static final double MIN_VELOCITY = -4;

    public static byte toAngle(float value) {
        return (byte) (value / 360 * 256);
    }

    public static short toVelocity(double value) {
        value = clamp(MIN_VELOCITY, value, MAX_VELOCITY);
        return (short) (value * 8000);
    }

    public static short toMovement(double delta) {
        return (short) (delta * 4096);
    }

    private static double clamp(double min, double value, double max) {
        return value < min ? min : Math.min(value, max);
    }
}
