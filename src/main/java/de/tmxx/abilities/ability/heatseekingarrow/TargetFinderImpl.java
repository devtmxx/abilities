package de.tmxx.abilities.ability.heatseekingarrow;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.abilities.wrapper.packet.EntityMetadataPacketWrapper;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Project: abilities
 * 12.03.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TargetFinderImpl extends BukkitRunnable implements TargetFinder {
    private static final double SEARCH_DISTANCE = 32.0D;
    private static final double MAX_SEARCH_ANGLE = Math.PI / 8;
    private static final String MARKER_TEAM_NAME = "marker_team";

    private final JavaPlugin plugin;
    private final Player player;

    private Team markerTeam = null;
    private Entity currentTarget = null;

    @Inject
    TargetFinderImpl(JavaPlugin plugin, @Assisted Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void startTracking() {
        setMarkerColor();

        runTaskTimer(plugin, 0L, 5L);
    }

    @Override
    public void stopTracking() {
        cancel();
        setMarker(false);
    }

    @Override
    public Entity getCurrentTarget() {
        return currentTarget;
    }

    @Override
    public void run() {
        Entity newTarget = getNearestTarget();
        if (currentTarget != null) {
            if (!currentTarget.equals(newTarget)) {
                setMarker(false);
            } else {
                return;
            }
        }

        currentTarget = newTarget;
        setMarker(true);
    }

    private Entity getNearestTarget() {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();

        List<Entity> entities = player.getNearbyEntities(SEARCH_DISTANCE, SEARCH_DISTANCE, SEARCH_DISTANCE);
        Entity closestEntity = null;
        double closestAngle = MAX_SEARCH_ANGLE;

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity.equals(player)) continue;

            Vector toEntity = entity.getLocation().toVector().subtract(eyeLocation.toVector()).normalize();
            double angle = direction.angle(toEntity);

            if (angle < closestAngle) {
                closestAngle = angle;
                closestEntity = entity;
            }
        }

        return closestEntity;
    }

    private void setMarker(boolean marker) {
        if (currentTarget == null) return;

        if (marker) {
            markerTeam.addEntity(currentTarget);
        } else {
            markerTeam.removeEntity(currentTarget);
        }

        EntityMetadataPacketWrapper packet = new EntityMetadataPacketWrapper(currentTarget.getEntityId());
        packet.setMetadata(0, (byte) (marker ? 0x40 : 0));

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet.getHandle());
    }

    private void setMarkerColor() {
        markerTeam = player.getScoreboard().getTeam(MARKER_TEAM_NAME);
        if (markerTeam == null) {
            markerTeam = player.getScoreboard().registerNewTeam(MARKER_TEAM_NAME);
            markerTeam.color(NamedTextColor.LIGHT_PURPLE);
        }
    }
}
