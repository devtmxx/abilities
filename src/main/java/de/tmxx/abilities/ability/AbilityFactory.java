package de.tmxx.abilities.ability;

import com.google.inject.assistedinject.Assisted;
import de.tmxx.abilities.ability.endershot.EnderShotProjectile;
import de.tmxx.abilities.ability.heatseekingarrow.HeatSeekingArrow;
import de.tmxx.abilities.ability.heatseekingarrow.TargetFinder;
import de.tmxx.abilities.ability.waterbender.WaterQueue;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

/**
 * Project: abilities
 * 10.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public interface AbilityFactory {
    WaterQueue newWaterQueue();
    EnderShotProjectile newEnderShotProjectile(Player shooter);
    HeatSeekingArrow newHeatSeekingArrow(@Assisted("shooter") Player shooter, @Assisted("target") Player target, Arrow arrow);
    TargetFinder newTargetFinder(Player player);
}
