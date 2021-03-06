package com.redcreator37.playermanagement.Commands.PlayerCommands;

import com.redcreator37.playermanagement.Commands.PlayerCommand;
import com.redcreator37.playermanagement.DataModels.Job;
import com.redcreator37.playermanagement.DataModels.ServerPlayer;
import com.redcreator37.playermanagement.Localization;
import com.redcreator37.playermanagement.PlayerManagement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * Sets the player's job
 */
public class SetJob extends PlayerCommand {

    public SetJob() {
        super("setjob", new LinkedHashMap<String, Boolean>() {{
            put("job_name", true);
            put("player_name", false);
        }}, new ArrayList<String>() {{
            add("management.user");
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
        Optional<ServerPlayer> optTarget = getUserOrAdmin(player, args, 1, 1);
        if (!optTarget.isPresent()) return;
        ServerPlayer target = optTarget.get();

        Job newJob = PlayerManagement.jobs.get(args[0]);
        if (newJob == null) {
            player.sendMessage(PlayerManagement.prefs.prefix
                    + MessageFormat.format(Localization.lc("unknown-job"), args[0]));
            return;
        }

        target.setJob(newJob);
        Bukkit.getScheduler().runTask(PlayerManagement
                .getPlugin(PlayerManagement.class), () -> {
            try {   // set the job and update the player list
                PlayerManagement.players.updatePlayerEntry(target);
                player.sendMessage(PlayerManagement.prefs.prefix
                        + MessageFormat.format(Localization.lc("now-employed-as"), target, newJob));
            } catch (SQLException e) {
                player.sendMessage(PlayerManagement.prefs.prefix + MessageFormat
                        .format(Localization.lc("error-updating-playerdata"), e.getMessage()));
            }
        });
    }
}
