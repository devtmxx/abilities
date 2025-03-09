package de.tmxx.abilities.entity;

import com.google.inject.Singleton;
import de.tmxx.abilities.AbilitiesPlugin;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
@Singleton
public class CustomEntityRegistry {
    private final Map<Integer, CustomEntity> entities = new HashMap<>();

    public @NotNull <T extends CustomEntity> T createEntity(Class<T> entityClass) {
        T entity = AbilitiesPlugin.injector().getInstance(entityClass);
        entities.put(entity.getEntityId(), entity);
        return entity;
    }

    public @Nullable CustomEntity getEntity(int entityId) {
        return entities.get(entityId);
    }

    public @Nullable <T extends CustomEntity> T getEntity(int entityId, Class<T> entityClass) {
        CustomEntity entity = getEntity(entityId);
        if (entity == null) return null;
        if (!entityClass.isAssignableFrom(entity.getClass())) return null;

        return entityClass.cast(entity);
    }

    public @NotNull List<CustomEntity> getEntitiesInWorld(@NotNull World world) {
        return entities.values().stream().filter(entity -> world.equals(entity.getWorld())).toList();
    }

    public void removeEntity(int entityId) {
        entities.remove(entityId);
    }

    public void removeEntity(@NotNull CustomEntity entity) {
        removeEntity(entity.getEntityId());
    }
}
