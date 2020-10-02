package com.redcreator37.playermanagement.Commands;

import com.redcreator37.playermanagement.DataModels.ServerPlayer;
import com.redcreator37.playermanagement.PlayerManagement;
import com.redcreator37.playermanagement.PlayerRoutines;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;

import static com.redcreator37.playermanagement.PlayerManagement.strings;

/**
 * Increases a player's punishment count, takes the amount of money
 * and bans them if their count exceeds the maximum
 */
public class LowerRank implements CommandExecutor {

    /**
     * Main command process
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        Player p = PlayerRoutines.playerFromSender(sender);
        if (p == null || !PlayerRoutines.checkPermission(p, "management.admin"))
            return true;

        if (args.length < 1) {
            p.sendMessage(PlayerManagement.prefix + CommandHelper
                    .parseCommandUsage("punish", new String[]{"*player_name", "Reason"}));
            return true;
        }

        ServerPlayer target = PlayerManagement.players.get(PlayerRoutines
                .uuidFromUsername(PlayerManagement.players, args[0]));
        if (PlayerRoutines.checkPlayerNonExistent(p, target, args[0]))
            return true;

        Bukkit.getScheduler().runTask(PlayerManagement
                .getPlugin(PlayerManagement.class), () -> {
            try {
                // take the amount of money
                try {
                    PlayerManagement.eco.withdrawPlayer(p.getServer().getPlayer(target
                            .getUsername()), PlayerManagement.punishmentAmount);
                } catch (Exception e) {
                    p.sendMessage(PlayerManagement.prefix + ChatColor.GOLD
                            + strings.getString("the-player")
                            + ChatColor.GREEN + target + ChatColor.GOLD
                            + strings.getString("isnt-online-money-not-taken"));
                }

                target.setPunishments(target.getPunishments() + 1);

                if (args.length > 1) {  // if there's a reason specified
                    String reason = CommandHelper.getFullEntry(args, 1);
                    try {   // tell them if they're online, otherwise ignore it
                        Objects.requireNonNull(p.getServer().getPlayer(target
                                .getUsername())).sendMessage(PlayerManagement
                                .prefix + ChatColor.GOLD + strings
                                .getString("you-have-been-punished")
                                + ChatColor.GREEN + reason);
                    } catch (NullPointerException ignored) {}
                }

                // limit exceeded, issue ban
                if (target.getPunishments() > PlayerManagement.maxPunishments) {
                    sender.getServer().getBannedPlayers().add(p.getServer()
                            .getPlayer(target.getUsername()));
                    p.sendMessage(PlayerManagement.prefix + ChatColor.GOLD
                            + strings.getString("the-player") + ChatColor.GREEN + target
                            + ChatColor.GOLD + strings.getString("has-exceeded-max-punishments"));
                } else {
                    p.sendMessage(PlayerManagement.prefix + ChatColor.GOLD
                            + strings.getString("the-player") + ChatColor.GREEN + target
                            + ChatColor.GOLD + strings.getString("has-been-punished"));
                }

                PlayerManagement.playerDb.update(target);
                PlayerManagement.players = PlayerManagement.playerDb.getAll();
            } catch (SQLException e) {
                p.sendMessage(PlayerManagement.prefix + ChatColor.GOLD
                        + strings.getString("error-updating-playerdata")
                        + ChatColor.RED + e.getMessage());
            }
        });
        return true;
    }
}
