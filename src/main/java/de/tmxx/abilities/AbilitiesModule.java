package de.tmxx.abilities;

import com.google.inject.AbstractModule;
import de.tmxx.abilities.entity.CustomEntityRegistry;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class AbilitiesModule extends AbstractModule {
    private final AbilitiesPlugin plugin;

    public AbilitiesModule(AbilitiesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(plugin);
        bind(CustomEntityRegistry.class);
    }
}
