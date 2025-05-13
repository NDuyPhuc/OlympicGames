package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.db.DBConnectionManager;
import com.duyphuc.olympics.model.OlympicEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OlympicEventDAO {

    public List<OlympicEvent> getAllEvents() {
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
        } catch (SQLException e) {
            System.err.println("Error fetching Olympic events: " + e.getMessage());
            // Consider throwing a custom DataAccessException
        }
        return events;
    }
}