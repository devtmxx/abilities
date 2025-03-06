package de.tmxx.abilities;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Project: abilities
 * 06.03.25
 *
 * @author timmauersberger
 * @version 1.0
 */
public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return true;

        FallDamageAbility.playAnimation(player.getLocation());
        sender.sendMessage("Test done");
        return true;
    }
}
