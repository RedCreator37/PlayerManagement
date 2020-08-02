package com.redcreator37.playermanagement.Database;

import com.redcreator37.playermanagement.DataModels.ServerPlayer;
import com.redcreator37.playermanagement.PlayerManagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Player-related database routines
 */
public final class PlayerDb {

    /**
     * Noninstantiable
     */
    private PlayerDb() {
    }

    /**
     * Executes the specified sql update query
     *
     * @param sql    the SQL command. Example: <code>INSERT INTO
     *               contacts (name, surname) VALUES (?, ?)</code>
     * @param player the player object to get the data from
     * @param db     the database connection
     * @throws SQLException on error
     */
    private static void runPlayerSqlUpdate(String sql, ServerPlayer player, Connection db, boolean update) throws SQLException {
        PreparedStatement st = db.prepareStatement(sql);
        st.closeOnCompletion();
        st.setString(1, player.getUsername());
        st.setString(2, player.getUuid());
        st.setString(3, player.getName());
        st.setString(4, player.getJoinDate());
        String job = "";    // because of a possible null pointer
        if (player.getJob() != null) job = player.getJob().getName();
        st.setString(5, job);
        st.setString(6, player.getCompany().getName());
        st.setString(7, player.getNotes());
        st.setInt(8, player.getPunishments());
        if (update) st.setInt(9, player.getId());
        st.executeUpdate();
    }

    /**
     * Runs the specified SQL query to return the list of
     * server players in the database
     *
     * @param sql sql query to run
     * @param db  the database connection
     * @return the list of players in the database
     * @throws SQLException on error
     */
    private static Map<String, ServerPlayer> commonPlayerQuery(String sql, Connection db) throws SQLException {
        Map<String, ServerPlayer> players = new HashMap<>();
        Statement st = db.createStatement();
        st.closeOnCompletion();
        ResultSet set = st.executeQuery(sql);

        // loop through the records
        while (set.next()) {
            ServerPlayer p = new ServerPlayer(set.getInt("id"),
                    set.getString("username"),
                    set.getString("uuid"));
            p.setName(set.getString("name"));
            p.setJoinDate(set.getString("join_date"));
            p.setJob(PlayerManagement.jobs.get(set.getString("job")));
            p.setCompany(PlayerManagement.companies.get(set.getString("company")));
            p.setNotes(set.getString("notes"));
            p.setPunishments(set.getInt("punishments"));
            players.put(p.getUuid(), p);
        }
        set.close();
        return players;
    }

    /**
     * Returns the list of all server players
     *
     * @param db the database connection
     * @return the player list
     * @throws SQLException on error
     */
    public static Map<String, ServerPlayer> getAllPlayers(Connection db) throws SQLException {
        String cmd = "SELECT players.* FROM players INNER JOIN jobs ON jobs.name = players.job" +
                " INNER JOIN companies ON companies.name = players.company;";
        return commonPlayerQuery(cmd, db);
    }

    /**
     * Returns the list of all server players, use only when
     * registering new players
     *
     * @param db the database connection
     * @return the player list
     * @throws SQLException on error
     */
    public static Map<String, ServerPlayer> getAllNewlyRegistered(Connection db) throws SQLException {
        String cmd = "SELECT * FROM players";
        return commonPlayerQuery(cmd, db);
    }

    /**
     * Adds another player to the database
     *
     * @param player the ServerPlayer object to be inserted
     * @param db     the database connection
     * @throws SQLException on error
     */
    public static void insertPlayer(ServerPlayer player, Connection db) throws SQLException {
        String cmd = "INSERT INTO players(username, uuid, name, join_date," +
                "job, company, notes, punishments) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        runPlayerSqlUpdate(cmd, player, db, false);
    }

    /**
     * Updates the data of an existing player in the database
     *
     * @param player the ServerPlayer object to be updated
     * @param db     the database connection
     * @throws SQLException on error
     */
    public static void updatePlayer(ServerPlayer player, Connection db) throws SQLException {
        String cmd = "UPDATE players SET username = ?, uuid = ?, name = ?," +
                "join_date = ?, job = ?, company = ?, notes = ?, punishments = ? " +
                "WHERE ID = ?";
        runPlayerSqlUpdate(cmd, player, db, true);
    }

    /**
     * Deletes the player with the specified id from the database
     *
     * @param id player id
     * @param db the database connection
     * @throws SQLException on error
     */
    public static void removePlayer(int id, Connection db) throws SQLException {
        String cmd = "DELETE FROM players WHERE id = " + id + ";";
        db.prepareStatement(cmd).executeUpdate();
    }

}
