package com.redcreator37.playermanagement.Database;

import com.redcreator37.playermanagement.DataModels.Transaction;
import com.redcreator37.playermanagement.PlayerManagement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction-related database routines
 */
public final class TransactionDb {

    /**
     * Noninstantiable
     */
    private TransactionDb() {
    }

    /**
     * Executes the specified sql update query
     *
     * @param sql the SQL command. Example: <code>INSERT INTO
     *            contacts (name, surname) VALUES (?, ?)</code>
     * @param t   the Transaction object to get the data from
     * @param db  the database connection
     * @throws SQLException on error
     */
    private static void runTransactionSqlUpdate(String sql, Transaction t, Connection db) throws SQLException {
        PreparedStatement st = db.prepareStatement(sql);
        st.closeOnCompletion();
        st.setInt(1, t.getCompanyId());
        st.setString(2, t.getDirection());
        st.setString(3, t.getTitle());
        st.setString(4, t.getDescription());
        st.setString(5, t.getAmount().toString());
        st.executeUpdate();
    }

    /**
     * Returns the list of all transactions in the database
     *
     * @param db the database connection
     * @return the transaction list
     * @throws SQLException on error
     */
    public static List<Transaction> getAllTransactions(Connection db) throws SQLException {
        String cmd = "SELECT * FROM transactions";
        List<Transaction> transactions = new ArrayList<>();
        ResultSet set = db.createStatement().executeQuery(cmd);

        // loop through the records
        while (set.next()) {
            Transaction t = new Transaction(set.getInt("id"),
                    set.getInt("companyId"),
                    set.getString("direction"),
                    set.getString("title"),
                    set.getString("description"),
                    new BigDecimal(set.getString("amount")));
            transactions.add(t);
        }
        return transactions;
    }

    /**
     * Adds another transaction to the database
     *
     * @param t  the Transaction object to be inserted
     * @param db the database connection
     * @throws SQLException on error
     */
    public static void insertTransaction(Transaction t, Connection db) throws SQLException {
        String cmd = "INSERT INTO transactions(companyId, direction, title," +
                "description, amount) VALUES(?, ?, ?, ?, ?)";
        runTransactionSqlUpdate(cmd, t, db);
    }

    /**
     * Adds a transaction in the background
     *
     * @param p  the player
     * @param t  the transaction
     * @param db the database connection
     */
    public static void addTransactionAsync(Player p, Transaction t, Connection db) {
        Bukkit.getScheduler().runTask(PlayerManagement
                .getPlugin(PlayerManagement.class), () -> {
            try {
                insertTransaction(t, db);
                p.sendMessage(PlayerManagement.prefix + ChatColor.GOLD
                        + "Transaction data saved.");
            } catch (SQLException e) {
                p.sendMessage(PlayerManagement.prefix + ChatColor.GOLD
                        + "Error while saving transaction" +
                        " data: " + ChatColor.RED
                        + e.getMessage());
            }
        });
    }

}
