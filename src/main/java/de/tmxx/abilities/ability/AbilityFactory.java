package de.tmxx.abilities.ability;

import de.tmxx.abilities.ability.endershot.EnderShotProjectile;
import de.tmxx.abilities.ability.heatseekingarrow.ArrowGuidance;
import de.tmxx.abilities.ability.heatseekingarrow.TargetFinder;
import de.tmxx.abilities.ability.tornado.Tornado;
import de.tmxx.abilities.ability.waterbender.WaterQueue;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Project: abilities
 * 10.03.2025
 *
 * <p>
 *     A factory to create ability related entities.
 * </p>
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface AbilityFactory {
    WaterQueue newWaterQueue();
    EnderShotProjectile newEnderShotProjectile(Player shooter);
    ArrowGuidance newArrowGuidance(Entity target, Arrow arrow);
    TargetFinder newTargetFinder(Player player);
    Tornado newTornado(Player player);
}
