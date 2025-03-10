package de.tmxx.abilities.ability.waterbender;

import com.google.inject.Inject;
import de.tmxx.abilities.entity.CustomEntityRegistry;
import de.tmxx.abilities.entity.CustomFallingBlock;
import de.tmxx.abilities.entity.FallingBlockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: abilities
 * 10.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class WaterQueueImpl implements WaterQueue {
    private static final int QUEUE_LENGTH = 20;
    private final CustomFallingBlock[] blocks = new CustomFallingBlock[QUEUE_LENGTH];
    private final List<Location> path = new ArrayList<>(QUEUE_LENGTH);

    private final JavaPlugin plugin;
    private final CustomEntityRegistry entityRegistry;

    @Inject
    public WaterQueueImpl(JavaPlugin plugin, CustomEntityRegistry entityRegistry) {
        this.plugin = plugin;
        this.entityRegistry = entityRegistry;
    }

    public void spawn(Location source) {
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = spawnBlock(source);
            path.add(source);
        }
    }

    public void addRoute(Location next) {
        path.removeFirst();
        path.add(next);

        updatePositions();
    }

    public void despawn() {
        for (CustomFallingBlock block : blocks) {
            block.remove();
            block.getLocation().getBlock().setType(Material.WATER);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (CustomFallingBlock block : blocks) {
                block.getLocation().getBlock().setType(Material.AIR);
            }
        }, 20L);
    }

    private CustomFallingBlock spawnBlock(Location sourceLocation) {
        CustomFallingBlock fallingBlock = entityRegistry.createEntity(CustomFallingBlock.class);
        fallingBlock.setType(FallingBlockType.BLUE_ICE);
        fallingBlock.setNoGravity(true);
        fallingBlock.spawn(sourceLocation);
        return fallingBlock;
    }

    private void updatePositions() {
        for (int i = 0; i < blocks.length; i++) {
            blocks[i].move(path.get(i));
        }
    }
}
