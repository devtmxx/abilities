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
 * <p>
 *     This handles the projectile animation and calculates whether an entity has been hit.
 * </p>
 *
 * @author timmauersberger
 * @version 1.0
 */
public class EnderShotProjectileImpl extends BukkitRunnable implements EnderShotProjectile {
    // Just some helper values for better readability
    private static final double PI = Math.PI;
    private static final double HALF_PI = PI / 2;
    private static final double TWO_PI = 2 * PI;

    // Values to calculate the projectiles angle
    private static final double ANGLE_STEPS = 12.0D;
    private static final double ANGLE_INCREASE = TWO_PI / ANGLE_STEPS;

    // Projectile values
    private static final double SPEED = 1.0D;
    private static final double LAUNCH_DISTANCE = 1.5D;
    private static final int TIMEOUT_STEP = 200;
    private static final int EXPLOSION_POWER = 1;
    private static final double PROJECTILE_DAMAGE = 10.0D;
    private static final double ENTITY_HIT_RADIUS = 0.5D;
    private static final double BLOCK_HIT_DISTANCE = 0.2D;

    private final JavaPlugin plugin;
    private final Player shooter;

    // Values to track the projectiles current state
    private Location launchLocation;
    private Vector launchDirection;
    private ArmorStand projectileHolder;

    // The current progress of the projectile
    private int currentStep = 0;

    // The current rotation of the projectile. The x and y multiplier's purpose is it to set the facing of the projectile
    // (either up, down or horizontal)
    private int yMultiplier = 0;
    private int zMultiplier = 0;
    private double currentAngle = 0;

    @Inject
    EnderShotProjectileImpl(JavaPlugin plugin, @Assisted Player shooter) {
        this.plugin = plugin;
        this.shooter = shooter;
    }

    /**
     * Launches the projectile.
     */
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

    /**
     * Periodic update loop to handle the projectile animation.
     */
    @Override
    public void run() {
        // Cancel if the projectile holder has been removed
        if (projectileHolder.isDead()) {
            cancel();
            return;
        }

        // Cancel if the current progress reaches the timeout limit
        if (currentStep == TIMEOUT_STEP) {
            projectileHolder.remove();
            cancel();
            return;
        }
        currentStep++;

        increaseCurrentAngle();

        // Play animation
        projectileHolder.setHeadPose(currentHeadPose());
        projectileHolder.setVelocity(launchDirection.clone());
        playSound();
        displayParticles();

        // Check if the projectile either hit an entity or a block and remove the projectile in each case.
        boolean remove = checkHitEntity() || checkHitBlock();

        if (remove) {
            createExplosion();
            projectileHolder.remove();
            cancel();
        }
    }

    /**
     * Spawns an armor stand as the projectile holder and teleports it to the launch location.
     */
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

    /**
     * Gives the spawn location for the projectile holder. This is 100 blocks above where the projectile should
     * actually launch. This is necessary because the projectile holder (armor stand) will be visible for a single tick,
     * and we don't want players to see that.
     */
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

    /**
     * Spawning particles at the exact projectile location will - for whatever reason - display the particles in front
     * of the actual projectile at a distance depending on the projectiles speed. To prevent this we subtract a multiple
     * of the projectiles direction from its position based on its speed.
     */
    private Location particleLocation() {
        return projectileHolder.getLocation().clone()
                .add(0, 0.75, 0)
                .subtract(launchDirection.clone().normalize().multiply(SPEED * 3));
    }

    /**
     * Checks if an entity has been hit.
     */
    private boolean checkHitEntity() {
        for (LivingEntity entity : world().getNearbyLivingEntities(projectileHolder.getLocation(), ENTITY_HIT_RADIUS)) {
            // Do not count a hit to the projectile holder as it will always be in reach
            if (entity.equals(projectileHolder)) continue;

            entity.damage(PROJECTILE_DAMAGE);
            return true;
        }

        return false;
    }

    /**
     * Check if a solid block has been hit.
     */
    private boolean checkHitBlock() {
        Location inFront = projectileHolder.getLocation().clone().add(launchDirection.clone().normalize().multiply(BLOCK_HIT_DISTANCE));
        return inFront.getBlock().isSolid();
    }

    private void createExplosion() {
        world().createExplosion(projectileHolder.getLocation(), EXPLOSION_POWER, true, true, shooter);
    }

    /**
     * Calculates the current head pose.
     */
    private EulerAngle currentHeadPose() {
        double x = pitchToAngle();
        double y = yMultiplier * currentAngle;
        double z = zMultiplier * currentAngle;
        return new EulerAngle(x, y, z);
    }

    private void increaseCurrentAngle() {
        currentAngle += ANGLE_INCREASE;
        if (currentAngle >= TWO_PI) currentAngle -= TWO_PI;
    }

    private double pitchToAngle() {
        float pitch = pitch();
        return pitch < 45 ? 0 : pitch <= 135 ? HALF_PI : PI;
    }

    private int yMultiplier() {
        float pitch = pitch();
        return pitch >= 45 && pitch <= 135 ? 0 : 1;
    }

    private int zMultiplier() {
        float pitch = pitch();
        return pitch >= 45 && pitch <= 135 ? 1 : 0;
    }

    /**
     * By adding 90 degrees to the launch locations pitch, we make calculations more understandable. This is necessary
     * because the facing of the projectile will not equal the facing of the launch location but rather its top, thus
     * plus 90 degrees.
     */
    private float pitch() {
        return launchLocation.getPitch() + 90;
    }
}
