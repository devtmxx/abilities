package de.tmxx.abilities;

import de.tmxx.abilities.entity.CustomFallingBlock;
import de.tmxx.abilities.entity.FallingBlockType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Project: abilities
 * 08.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class WaterBenderAbility implements Listener {
    private final Set<Player> active = new HashSet<>();
    private final Map<UUID, CustomFallingBlock> fallingBlockIds = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            if (active.contains(event.getPlayer())) {
                active.remove(event.getPlayer());
            } else {
                active.add(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!active.contains(event.getPlayer())) return;

        UUID uniqueId = event.getPlayer().getUniqueId();
        if (!fallingBlockIds.containsKey(uniqueId)) {
            CustomFallingBlock fallingBlock = new CustomFallingBlock();
            fallingBlock.setType(FallingBlockType.BLUE_ICE);
            fallingBlock.setNoGravity(true);
            fallingBlock.spawn(locationFromView(event.getPlayer()));
            fallingBlockIds.put(uniqueId, fallingBlock);
            return;
        }

        CustomFallingBlock fallingBlock = fallingBlockIds.get(uniqueId);
        fallingBlock.move(locationFromView(event.getPlayer()));
    }

    private Location locationFromView(Player player) {
        int radius = 10;
        Vector direction = player.getEyeLocation().getDirection().clone().normalize();
        direction.multiply(radius);
        return player.getEyeLocation().clone().add(direction);
    }
}
