package de.tmxx.abilities.ability;

import com.google.inject.Inject;
import de.tmxx.abilities.ability.tornado.Tornado;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Project: abilities
 * 13.03.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TornadoAbility implements Ability, Listener {
    private final JavaPlugin plugin;
    private final AbilityFactory factory;
    private final Map<UUID, Tornado> tornados = new HashMap<>();

    @Inject
    TornadoAbility(JavaPlugin plugin, AbilityFactory factory) {
        this.plugin = plugin;
        this.factory = factory;
    }

    @Override
    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        UUID uniqueId = event.getPlayer().getUniqueId();
        Tornado tornado;
        if (!tornados.containsKey(uniqueId)) {
            tornados.put(uniqueId, tornado = factory.newTornado(event.getPlayer()));
        } else {
            tornado = tornados.get(uniqueId);
        }

       if (event.isSneaking()) {
           tornado.play();
       } else {
           tornado.pause();
       }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Tornado tornado = tornados.remove(event.getPlayer().getUniqueId());
        if (tornado != null) tornado.stop();
    }
}
