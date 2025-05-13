package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.db.DBConnectionManager;
import com.duyphuc.olympics.model.MedalEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedalDAO {

    // Phương thức này sử dụng String.format. Đảm bảo tableName là an toàn.
    public List<MedalEntry> getMedalsByEventTable(String tableName) {
        List<MedalEntry> entries = new ArrayList<>();
        // Sanitize tableName to prevent SQL injection if it were from untrusted source
        // For this project, tableName comes from olympic_events table, so it's considered safe.
        // However, for a general utility, more robust validation would be needed.
        if (!tableName.matches("^[a-zA-Z0-9_]+$")) {
             System.err.println("Invalid table name format: " + tableName);
             return entries; // or throw an exception
        }

        String sql = String.format("SELECT id, NOC, Gold, Silver, Bronze, Total FROM %s ORDER BY Gold DESC, Silver DESC, Bronze DESC, NOC ASC", tableName);

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                MedalEntry entry = new MedalEntry();
                entry.setId(rs.getInt("id"));
                entry.setNoc(rs.getString("NOC"));
                entry.setGold(rs.getInt("Gold"));
                entry.setSilver(rs.getInt("Silver"));
                entry.setBronze(rs.getInt("Bronze"));
                entry.setTotal(rs.getInt("Total")); // Hoặc để MedalEntry tự tính
                entries.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching medals from table " + tableName + ": " + e.getMessage());
        }
        return entries;
    }

    public boolean addMedalEntry(MedalEntry entry, String tableName) {
        if (!tableName.matches("^[a-zA-Z0-9_]+$")) {
             System.err.println("Invalid table name format: " + tableName);
             return false;
        }
        String sql = String.format("INSERT INTO %s (NOC, Gold, Silver, Bronze, Total) VALUES (?, ?, ?, ?, ?)", tableName);
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, entry.getNoc());
            pstmt.setInt(2, entry.getGold());
            pstmt.setInt(3, entry.getSilver());
            pstmt.setInt(4, entry.getBronze());
            pstmt.setInt(5, entry.getTotal()); // Đảm bảo total đã được tính

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entry.setId(generatedKeys.getInt(1)); // Lấy ID tự tăng
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding medal entry to " + tableName + ": " + e.getMessage());
        }
        return false;
    }

    public boolean updateMedalEntry(MedalEntry entry, String tableName) {
        if (!tableName.matches("^[a-zA-Z0-9_]+$")) {
             System.err.println("Invalid table name format: " + tableName);
             return false;
        }
        String sql = String.format("UPDATE %s SET NOC = ?, Gold = ?, Silver = ?, Bronze = ?, Total = ? WHERE id = ?", tableName);
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getNoc());
            pstmt.setInt(2, entry.getGold());
            pstmt.setInt(3, entry.getSilver());
            pstmt.setInt(4, entry.getBronze());
            pstmt.setInt(5, entry.getTotal());
            pstmt.setInt(6, entry.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medal entry in " + tableName + ": " + e.getMessage());
        }
        return false;
    }

    public boolean deleteMedalEntry(int entryId, String tableName) {
         if (!tableName.matches("^[a-zA-Z0-9_]+$")) {
             System.err.println("Invalid table name format: " + tableName);
             return false;
        }
        String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting medal entry from " + tableName + ": " + e.getMessage());
        }
        return false;
    }
}