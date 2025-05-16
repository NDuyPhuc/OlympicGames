package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.db.DBConnectionManager;
import com.duyphuc.olympics.model.OlympicEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OlympicEventDAO {

    public List<OlympicEvent> getAllEvents() throws SQLException {
        List<OlympicEvent> events = new ArrayList<>();
        String sql = "SELECT id, event_name, year, event_type, table_name_in_db FROM olympic_events ORDER BY year DESC, event_name";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                events.add(new OlympicEvent(
                        rs.getInt("id"),
                        rs.getString("event_name"),
                        rs.getInt("year"),
                        rs.getString("event_type"),
                        rs.getString("table_name_in_db")
                ));
            }
        }
        return events;
    }

    /**
     * Adds a new Olympic event to the olympic_events table.
     * table_name_in_db is NOT set here; it will be updated later.
     * @param event The OlympicEvent object (must have eventName, year, eventType).
     * @param conn The database connection (transaction managed by service).
     * @return The auto-generated ID of the new event.
     * @throws SQLException if a database access error occurs.
     */
    public int addOlympicEvent(OlympicEvent event, Connection conn) throws SQLException {
        // Insert only the initial details. table_name_in_db will be updated later.
        String sql = "INSERT INTO olympic_events (event_name, year, event_type) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, event.getEventName());
            pstmt.setInt(2, event.getYear());
            pstmt.setString(3, event.getEventType());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating Olympic event failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Updates the table_name_in_db for an existing Olympic event.
     * @param eventId The ID of the event to update.
     * @param tableName The new table name.
     * @param conn The database connection (transaction managed by service).
     * @throws SQLException if a database access error occurs.
     */
    public void updateOlympicEventTableName(int eventId, String tableName, Connection conn) throws SQLException {
        String sql = "UPDATE olympic_events SET table_name_in_db = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tableName);
            pstmt.setInt(2, eventId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating Olympic event table name failed, no rows affected for event ID: " + eventId);
            }
        }
    }

    public void deleteOlympicEventById(int eventId, Connection conn) throws SQLException {
        String sql = "DELETE FROM olympic_events WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.executeUpdate();
            // No need to check affectedRows strictly, as it might have been deleted by another process,
            // but the drop table operation is more critical.
        }
    }
}