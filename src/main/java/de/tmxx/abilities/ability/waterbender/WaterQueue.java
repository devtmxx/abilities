package de.tmxx.abilities.ability.waterbender;

import org.bukkit.Location;

/**
 * Project: abilities
 * 10.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface WaterQueue {
    void spawn(Location source);
    void addRoute(Location next);
    void despawn();
}
