package de.tmxx.abilities.ability.heatseekingarrow;

import org.bukkit.entity.Entity;

/**
 * Project: abilities
 * 12.03.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface TargetFinder {
    void startTracking();
    void stopTracking();
    Entity getCurrentTarget();
}
