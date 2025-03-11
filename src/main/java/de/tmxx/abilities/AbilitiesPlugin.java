package de.tmxx.abilities;

import com.google.common.reflect.ClassPath;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.tmxx.abilities.ability.Ability;
import de.tmxx.abilities.util.BlockStateIDLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;

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

        BlockStateIDLoader loader = injector.getInstance(BlockStateIDLoader.class);
        loader.loadBlockStateIDs();

        registerAbilities();
    }

    private void registerAbilities() {
        try {
            ClassPath.from(getClassLoader()).getTopLevelClasses(Ability.class.getPackageName()).forEach(classInfo -> {
                Class<?> clazz = classInfo.load();
                if (!Ability.class.isAssignableFrom(clazz) || Ability.class.equals(clazz)) return;

                Class<? extends Ability> abilityClass = clazz.asSubclass(Ability.class);
                Ability ability = injector.getInstance(abilityClass);
                ability.register();
            });
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not register abilities", e);
        }
    }
}
