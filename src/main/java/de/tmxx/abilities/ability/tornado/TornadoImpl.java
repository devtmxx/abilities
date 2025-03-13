package de.tmxx.abilities.ability.tornado;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.abilities.wrapper.packet.WorldParticlesPacketWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Project: abilities
 * 13.03.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TornadoImpl implements Tornado, Runnable {
    private static final int RINGS = 10;
    private static final Vector SLOPE = new Vector(1, 1.5, 0).normalize().multiply(0.5);
    private static final Map<Integer, List<Vector>> PARTICLE_RINGS;
    private static final double TORNADO_RANGE = 5.0D;
    private static final double VELOCITY_Y = 0.3D;
    private static final double VELOCITY_MULTIPLIER = 0.5D;
    private static final int PARTICLE_VIEW_DISTANCE = 64;

    private final JavaPlugin plugin;
    private final Player player;
    private final Map<Integer, Integer> states = new HashMap<>();

    private int taskId = -1;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Inject
    TornadoImpl(JavaPlugin plugin, @Assisted Player player) {
        this.plugin = plugin;
        this.player = player;

        setStartingStates();
    }

    @Override
    public void run() {
        if (!running.get()) return;

        throwEntitiesAround();
        playParticles();
        nextStates();
    }

    @Override
    public void play() {
        if (taskId == -1) taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 2L);
        running.set(true);
    }

    @Override
    public void pause() {
        running.set(false);
    }

    @Override
    public void stop() {
        running.set(false);
        Bukkit.getScheduler().cancelTask(taskId);
    }

    @Override
    public boolean isPlaying() {
        return running.get();
    }

    private void throwEntitiesAround() {
        player.getNearbyEntities(TORNADO_RANGE, TORNADO_RANGE, TORNADO_RANGE).stream()
                .filter(entity -> !entity.equals(player))
                .forEach(entity -> {
                    Location entityLocation = entity.getLocation();
                    Vector toEntity = entityLocation.toVector().subtract(player.getLocation().toVector()).normalize();
                    Vector velocity = new Vector(-toEntity.getZ(), VELOCITY_Y, toEntity.getX()).multiply(VELOCITY_MULTIPLIER);
                    entity.setVelocity(velocity);
                });
    }

    private void playParticles() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        for (int i = 0; i < RINGS; i++) {
            int state = states.get(i);
            Vector relative = PARTICLE_RINGS.get(i).get(state);

            WorldParticlesPacketWrapper packet = new WorldParticlesPacketWrapper(
                    player.getLocation().toVector().add(relative),
                    Particle.CLOUD
            );
            packet.setSpeed(0.0F);
            packet.setCount(1);
            protocolManager.broadcastServerPacket(packet.getHandle(), player.getLocation(), PARTICLE_VIEW_DISTANCE);
        }
    }

    private void setStartingStates() {
        for (int i = 0; i < PARTICLE_RINGS.size(); i++) {
            int startPosition = (int) (Math.random() * PARTICLE_RINGS.get(i).size());
            states.put(i, startPosition);
        }
    }

    private void nextStates() {
        for (int i = 0; i < PARTICLE_RINGS.size(); i++) {
            int currentState = states.get(i);
            if (++currentState >= PARTICLE_RINGS.get(i).size()) currentState = 0;
            states.put(i, currentState);
        }
    }

    static {
        PARTICLE_RINGS = new HashMap<>();

        Vector base = new Vector(0, 0, 0);
        for (int i = 0; i < RINGS; i++) {
            List<Vector> list = new ArrayList<>();
            float angle = (float) (Math.PI / (i * 2));
            base.add(SLOPE);

            for (int j = 0; j < i * 4; j++) {
                list.add(base.clone().rotateAroundY(angle * j));
            }

            PARTICLE_RINGS.put(i, list);
        }
    }
}
