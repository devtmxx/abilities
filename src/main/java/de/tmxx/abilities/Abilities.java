package de.tmxx.abilities;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Project: abilities
 * 06.03.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class Abilities extends JavaPlugin {
    @Override
    public void onEnable() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new FallDamageAbility(), this);

        Objects.requireNonNull(getCommand("test")).setExecutor(new TestCommand());
    }
}
