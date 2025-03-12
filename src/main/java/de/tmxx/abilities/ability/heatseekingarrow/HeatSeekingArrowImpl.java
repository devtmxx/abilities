package de.tmxx.abilities.ability.heatseekingarrow;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Project: abilities
 * 12.03.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class HeatSeekingArrowImpl extends BukkitRunnable implements HeatSeekingArrow {
    private final Player shooter;
    private final Player target;
    private final Arrow arrow;

    @Inject
    HeatSeekingArrowImpl(@Assisted("shooter") Player shooter, @Assisted("target") Player target, @Assisted Arrow arrow) {
        this.shooter = shooter;
        this.target = target;
        this.arrow = arrow;
    }

    @Override
    public void run() {

    }
}
