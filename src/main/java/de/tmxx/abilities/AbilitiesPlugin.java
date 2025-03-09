package de.tmxx.abilities;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public class AbilitiesPlugin extends JavaPlugin {
    private static Injector injector = null;

    public static @NotNull Injector injector() {
        if (injector == null) throw new IllegalStateException("Trying to access the injector before the plugin has been enabled");
        return injector;
    }

    @Override
    public void onEnable() {
        injector = Guice.createInjector(new AbilitiesModule(this));

        Bukkit.getPluginManager().registerEvents(new WaterBenderAbility(), this);
    }
}
