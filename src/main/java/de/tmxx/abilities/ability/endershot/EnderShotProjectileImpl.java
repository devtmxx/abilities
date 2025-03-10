package de.tmxx.abilities.ability.endershot;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.Vector3F;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.tmxx.abilities.wrapper.packet.WorldParticlesPacketWrapper;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Project: abilities
 * 10.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class EnderShotProjectileImpl extends BukkitRunnable implements EnderShotProjectile {
    private static final double SPEED = 1.0D;
    private static final double ANGLE_STEPS = 6.0D;
    private static final double ANGLE_INCREASE = Math.PI / ANGLE_STEPS;
    private static final double LAUNCH_DISTANCE = 1.5D;
    private static final int TIMEOUT_STEP = 200;
    private static final int EXPLOSION_POWER = 1;
    private static final double PROJECTILE_DAMAGE = 10.0D;
    private static final double ENTITY_HIT_RADIUS = 0.5D;
    private static final double BLOCK_HIT_DISTANCE = 0.2D;

    private final JavaPlugin plugin;
    private final Player shooter;

    private Location launchLocation;
    private Vector launchDirection;
    private ArmorStand projectileHolder;

    private int currentStep = 0;
    private int yMultiplier = 0;
    private int zMultiplier = 0;
    private double currentAngle = 0;

    @Inject
    public EnderShotProjectileImpl(JavaPlugin plugin, @Assisted Player shooter) {
        this.plugin = plugin;
        this.shooter = shooter;
    }

    @Override
    public void launch() {
        launchDirection = shooter.getEyeLocation().getDirection().clone().normalize().multiply(SPEED);
        launchLocation = shooter.getEyeLocation().clone()
                .subtract(0, 0.8, 0)
                .add(launchDirection.clone().normalize().multiply(LAUNCH_DISTANCE));

        yMultiplier = yMultiplier();
        zMultiplier = zMultiplier();

        projectileHolder = spawn();

        runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void run() {
        if (projectileHolder.isDead()) {
            cancel();
            return;
        }

        if (currentStep == TIMEOUT_STEP) {
            projectileHolder.remove();
            cancel();
            return;
        }
        currentStep++;

        increaseCurrentAngle();

        projectileHolder.setHeadPose(currentHeadPose());
        projectileHolder.setVelocity(launchDirection.clone());
        playSound();
        displayParticles();

        boolean remove = checkHitEntity() || checkHitBlock();

        if (remove) {
            createExplosion();
            projectileHolder.remove();
            cancel();
        }
    }

    private ArmorStand spawn() {
        ArmorStand armorStand = world().spawn(spawnLocation(), ArmorStand.class);
        armorStand.setInvisible(true);
        armorStand.setBasePlate(false);
        armorStand.setGravity(true);
        armorStand.setSmall(true);
        armorStand.setItem(EquipmentSlot.HEAD, ItemStack.of(Material.DRAGON_EGG));
        armorStand.setHeadPose(currentHeadPose());
        armorStand.teleport(launchLocation);
        return armorStand;
    }

    private World world() {
        return launchLocation.getWorld();
    }

    private Location spawnLocation() {
        return launchLocation.clone().add(0, 100, 0);
    }

    private void playSound() {
        world().playSound(projectileHolder.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5F, 2F);
    }

    private void displayParticles() {
        Location location = particleLocation();
        WorldParticlesPacketWrapper packet = new WorldParticlesPacketWrapper(
                new Vector(location.getX(), location.getY(), location.getZ()),
                Particle.CAMPFIRE_COSY_SMOKE
        );
        packet.setLongDistance(true);
        packet.setAlwaysVisible(true);
        packet.setOffset(new Vector3F(0.2F, 0.2F, 0.2F));
        packet.setSpeed(0);
        packet.setCount(5);

        ProtocolLibrary.getProtocolManager().broadcastServerPacket(
                packet.getHandle(),
                world().getPlayers()
        );
    }

    private Location particleLocation() {
        return projectileHolder.getLocation().clone()
                .add(0, 0.75, 0)
                .subtract(launchDirection.clone().normalize().multiply(SPEED * 3));
    }

    private boolean checkHitEntity() {
        for (LivingEntity entity : world().getNearbyLivingEntities(projectileHolder.getLocation(), ENTITY_HIT_RADIUS)) {
            if (entity.equals(projectileHolder)) continue;
            entity.damage(PROJECTILE_DAMAGE);
            return true;
        }

        return false;
    }

    private boolean checkHitBlock() {
        Location inFront = projectileHolder.getLocation().clone().add(launchDirection.clone().normalize().multiply(BLOCK_HIT_DISTANCE));
        return inFront.getBlock().isSolid();
    }

    private void createExplosion() {
        world().createExplosion(projectileHolder.getLocation(), EXPLOSION_POWER, true, true, shooter);
    }

    private EulerAngle currentHeadPose() {
        double x = pitchToAngle();
        double y = yMultiplier * currentAngle;
        double z = zMultiplier * currentAngle;
        return new EulerAngle(x, y, z);
    }

    private void increaseCurrentAngle() {
        currentAngle += ANGLE_INCREASE;
        if (currentAngle >= Math.PI * 2) currentAngle -= Math.PI * 2;
    }

    private double pitchToAngle() {
        float pitch = pitch();
        return pitch < 45 ? 0 : pitch <= 135 ? Math.PI / 2 : Math.PI;
    }

    private int yMultiplier() {
        float pitch = pitch();
        return pitch >= 45 && pitch <= 135 ? 0 : 1;
    }

    private int zMultiplier() {
        float pitch = pitch();
        return pitch >= 45 && pitch <= 135 ? 1 : 0;
    }

    private float pitch() {
        return launchLocation.getPitch() + 90;
    }
}
