package de.tmxx.abilities;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Project: abilities
 * 07.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TornadoAbility implements Listener, Runnable {
    private static final Map<Integer, List<Vector>> PARTICLE_RINGS;

    private final Set<Player> active = new HashSet<>();
    private final Map<Integer, Integer> particleStates = new HashMap<>();

    public TornadoAbility(Plugin plugin) {
        for (int i = 0; i < PARTICLE_RINGS.size(); i++) {
            int startPos = (int) (Math.random() * PARTICLE_RINGS.get(i).size());
            particleStates.put(i, startPos);
        }

        Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 2L);
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            active.add(event.getPlayer());
        } else {
            active.remove(event.getPlayer());
        }
    }

    @Override
    public void run() {
        active.forEach(this::playTornado);

        for (int i = 0; i < PARTICLE_RINGS.size(); i++) {
            int state = particleStates.get(i);
            if (++state >= PARTICLE_RINGS.get(i).size()) state = 0;
            particleStates.put(i, state);
        }
    }

    private void playTornado(Player player) {
        Location center = player.getLocation();

        World world = center.getWorld();
        List<Entity> entities = world.getNearbyEntities(center, 5, 5, 5).stream()
                .filter(e -> !e.equals(player))
                .toList();

        for (Entity entity : entities) {
            Location loc = entity.getLocation();
            Vector dir = loc.toVector().subtract(center.toVector()).normalize();

            // Set horizontal velocity (rotation around player)
            Vector velocity = new Vector(-dir.getZ(), 0.5, dir.getX()).multiply(0.5);

            entity.setVelocity(velocity);
        }

        for (int i = 0; i < PARTICLE_RINGS.size(); i++) {
            int state = particleStates.get(i);
            List<Vector> ringPositions = PARTICLE_RINGS.get(i);
            Vector vector = ringPositions.get(state);

            sendParticle(center.clone().add(vector));
        }
    }

    private void sendParticle(Location location) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.WORLD_PARTICLES);

        packet.getBooleans().write(0, false);
        packet.getBooleans().write(1, true);
        packet.getNewParticles().write(0, WrappedParticle.create(Particle.CLOUD, null));
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());
        packet.getFloat().write(0, 0f); // Offset X
        packet.getFloat().write(1, 0f); // Offset Y
        packet.getFloat().write(2, 0f); // Offset Z
        packet.getFloat().write(3, 0f); // Speed
        packet.getIntegers().write(0, 1); // Particle count

        protocolManager.broadcastServerPacket(packet);
    }

    static {
        PARTICLE_RINGS = new HashMap<>();

        int rings = 10;
        Vector slope = new Vector(1, 1.5, 0).normalize().multiply(0.5);
        Vector base = new Vector(0, 0, 0);

        for (int i = 1; i <= rings; i++) {
            List<Vector> list = new ArrayList<>();
            float angle = (float) (Math.PI / (i * 2));
            Vector ringBase = base.add(slope);

            for (int j = 0; j < i * 4; j++) {
                list.add(ringBase.clone().rotateAroundY(angle * j));
            }

            PARTICLE_RINGS.put(i - 1, list);
        }
    }
}
