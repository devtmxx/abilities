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
 * <p>
 *     This ability allows players to summon a tornado while sneaking.
 * </p>
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
        // Registers this ability as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles player sneaking to activate or deactivate the tornado.
     */
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        UUID uniqueId = event.getPlayer().getUniqueId();
        Tornado tornado;

        // Create a new tornado instance of the player doesn't have one yet
        if (!tornados.containsKey(uniqueId)) {
            tornados.put(uniqueId, tornado = factory.newTornado(event.getPlayer()));
        } else {
            tornado = tornados.get(uniqueId);
        }

        // Activate tornado when sneaking, pause when stopping
       if (event.isSneaking()) {
           tornado.play();
       } else {
           tornado.pause();
       }
    }

    /**
     * Ensures the tornado effect stops when the player quits.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Tornado tornado = tornados.remove(event.getPlayer().getUniqueId());
        if (tornado != null) tornado.stop();
    }
}
