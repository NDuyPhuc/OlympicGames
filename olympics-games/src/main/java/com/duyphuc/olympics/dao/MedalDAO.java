package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.db.DBConnectionManager;
import com.duyphuc.olympics.model.MedalEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Added for CREATE/DROP
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedalDAO {

    // --- Existing methods (getMedalsByEventTable, addMedalEntry, etc.) ---
    // Ensure methods like addMedalEntry, updateMedalEntry, deleteMedalEntry
    // can accept a Connection parameter if they are to be part of a larger transaction,
    // or they can continue to manage their own connections if they are standalone operations.
    // For simplicity here, I'll assume they manage their own connections for CRUD on medal entries,
    // but create/drop table will be part of a service-managed transaction.

    public List<MedalEntry> getMedalsByEventTable(String tableName) throws SQLException {
        List<MedalEntry> medals = new ArrayList<>();
        // Sanitize table name slightly, though it should be system-generated
        if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
            System.err.println("Invalid table name format: " + tableName);
            return Collections.emptyList();
        }
        String sql = "SELECT id, noc, gold, silver, bronze, total FROM " + tableName + " ORDER BY total DESC, gold DESC, noc ASC";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                medals.add(new MedalEntry(
                        rs.getInt("id"),
                        rs.getString("noc"),
                        rs.getInt("gold"),
                        rs.getInt("silver"),
                        rs.getInt("bronze")
                        // total is set by constructor or setters
                ));
            }
        }
        // SQLException will be thrown upwards
        return medals;
    }

    public boolean addMedalEntry(MedalEntry entry, String tableName) throws SQLException {
        if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
             System.err.println("Invalid table name for add: " + tableName);
             return false;
        }
        // Ensure total is calculated
        entry.setTotal(entry.getGold() + entry.getSilver() + entry.getBronze());

        String sql = "INSERT INTO " + tableName + " (noc, gold, silver, bronze, total) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, entry.getNoc());
            pstmt.setInt(2, entry.getGold());
            pstmt.setInt(3, entry.getSilver());
            pstmt.setInt(4, entry.getBronze());
            pstmt.setInt(5, entry.getTotal());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entry.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean updateMedalEntry(MedalEntry entry, String tableName) throws SQLException {
        if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
            System.err.println("Invalid table name for update: " + tableName);
            return false;
        }
        // Ensure total is calculated
        entry.setTotal(entry.getGold() + entry.getSilver() + entry.getBronze());
        String sql = "UPDATE " + tableName + " SET noc = ?, gold = ?, silver = ?, bronze = ?, total = ? WHERE id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getNoc());
            pstmt.setInt(2, entry.getGold());
            pstmt.setInt(3, entry.getSilver());
            pstmt.setInt(4, entry.getBronze());
            pstmt.setInt(5, entry.getTotal());
            pstmt.setInt(6, entry.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteMedalEntry(int entryId, String tableName) throws SQLException {
         if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
            System.err.println("Invalid table name for delete: " + tableName);
            return false;
        }
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public List<String> getNOCsByEventTable(String tableName) throws SQLException {
        List<String> nocs = new ArrayList<>();
        if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
            System.err.println("Invalid table name for getting NOCs: " + tableName);
            return nocs;
        }
        String sql = "SELECT DISTINCT noc FROM " + tableName + " ORDER BY noc";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                nocs.add(rs.getString("noc"));
            }
        }
        return nocs;
    }

    // --- New methods for table creation/deletion ---

    /**
     * Creates a new medal table for an Olympic event.
     * @param tableName The name of the table to create.
     * @param conn The database connection (transaction managed by service).
     * @throws SQLException if a database access error occurs.
     */
    public void createMedalTable(String tableName, Connection conn) throws SQLException {
        // Basic sanitization for table name (should be system-generated)
        if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
            throw new SQLException("Invalid table name format for creation: " + tableName);
        }
        String sql = "CREATE TABLE " + tableName + " ("
                   + "id INT AUTO_INCREMENT PRIMARY KEY,"
                   + "noc VARCHAR(3) NOT NULL,"
                   + "gold INT DEFAULT 0,"
                   + "silver INT DEFAULT 0,"
                   + "bronze INT DEFAULT 0,"
                   + "total INT DEFAULT 0,"
                   + "UNIQUE (noc)"
                   + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Table " + tableName + " created successfully.");
        }
    }

    /**
     * Drops an existing medal table.
     * @param tableName The name of the table to drop.
     * @param conn The database connection (transaction managed by service).
     * @throws SQLException if a database access error occurs.
     */
    public void dropMedalTable(String tableName, Connection conn) throws SQLException {
        // Basic sanitization
        if (tableName == null || !tableName.matches("^[a-zA-Z0-9_]+$")) {
            throw new SQLException("Invalid table name format for drop: " + tableName);
        }
        String sql = "DROP TABLE IF EXISTS " + tableName; // Use IF EXISTS for safety
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Table " + tableName + " dropped successfully.");
        }
    }
}