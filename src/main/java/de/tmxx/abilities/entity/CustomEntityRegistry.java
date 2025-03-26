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

    /**
     * Creates a new entity using the given entity class and registers it. This is necessary to display the custom
     * entity to players newly moving into its view distance.
     */
    public @NotNull <T extends CustomEntity> T createEntity(Class<T> entityClass) {
        T entity = AbilitiesPlugin.injector().getInstance(entityClass);
        entities.put(entity.getEntityId(), entity);
        return entity;
    }

    /**
     * Get a custom entity by the given entity id.
     */
    public @Nullable CustomEntity getEntity(int entityId) {
        return entities.get(entityId);
    }

    /**
     * Get a custom entity by the given entity id and class. This will return null if there is no entity with the given
     * id or the entity is not of the given class type.
     */
    public @Nullable <T extends CustomEntity> T getEntity(int entityId, Class<T> entityClass) {
        CustomEntity entity = getEntity(entityId);
        if (entity == null) return null;
        if (!entityClass.isAssignableFrom(entity.getClass())) return null;

        return entityClass.cast(entity);
    }

    /**
     * Get all custom entities within a given world.
     */
    public @NotNull List<CustomEntity> getEntitiesInWorld(@NotNull World world) {
        return entities.values().stream().filter(entity -> world.equals(entity.getWorld())).toList();
    }

    /**
     * Remove an entity by its id.
     */
    public void removeEntity(int entityId) {
        entities.remove(entityId);
    }

    /**
     * Remove an entity.
     */
    public void removeEntity(@NotNull CustomEntity entity) {
        removeEntity(entity.getEntityId());
    }
}
