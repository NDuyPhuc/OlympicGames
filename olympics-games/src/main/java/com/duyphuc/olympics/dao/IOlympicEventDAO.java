package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.model.OlympicEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IOlympicEventDAO {
    List<OlympicEvent> getAllEvents() throws SQLException;
    int addOlympicEvent(OlympicEvent event, Connection conn) throws SQLException;
    void updateOlympicEventTableName(int eventId, String tableName, Connection conn) throws SQLException;
    void deleteOlympicEventById(int eventId, Connection conn) throws SQLException;
}