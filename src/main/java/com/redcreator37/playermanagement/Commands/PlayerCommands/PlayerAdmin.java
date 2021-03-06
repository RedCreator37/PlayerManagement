package com.redcreator37.playermanagement.Commands.PlayerCommands;

import com.redcreator37.playermanagement.Commands.PlayerCommand;
import com.redcreator37.playermanagement.Localization;
import com.redcreator37.playermanagement.PlayerManagement;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import static com.redcreator37.playermanagement.PlayerRoutines.truncate;

/**
 * Displays all player data from the database
 */
public class PlayerAdmin extends PlayerCommand {

    public PlayerAdmin() {
        super("playeradmin", new LinkedHashMap<>(), new ArrayList<String>() {{
            add("management.admin");
        }});
    }

    /**
     * Runs this command and performs the actions
     *
     * @param player   the {@link Player} who ran the command
     * @param args     the arguments entered by the player
     * @param executor the UUID of the executing player
     */
    @Override
    public void execute(Player player, String[] args, UUID executor) {
        player.sendMessage(ChatColor.BLUE + "-----------------------------------------------------");
        player.sendMessage(Localization.lc("playerlist-header"));
        player.sendMessage(ChatColor.BLUE + "-----------------------------------------------------");
        PlayerManagement.players.getPlayers().forEach((s, pl) -> {
            StringBuilder b = new StringBuilder();
            String id = String.valueOf(pl.getId());
            if (id.length() < 2) id += " ";
            b.append(id).append(" | ");
            b.append(truncate(pl.getUsername(), 14)).append(" | ");
            b.append(truncate(pl.getJoinDate(), 10)).append(" | ");
            pl.getJob().ifPresent(job ->
                    b.append(truncate(job.toString(), 14)).append(" | "));
            pl.getCompany().ifPresent(company ->
                    b.append(truncate(company.toString(), 10)).append(" | "));
            b.append(pl.getPunishments());
            player.sendMessage(b.toString());
        });
        player.sendMessage(ChatColor.BLUE + "-----------------------------------------------------");
    }
}
