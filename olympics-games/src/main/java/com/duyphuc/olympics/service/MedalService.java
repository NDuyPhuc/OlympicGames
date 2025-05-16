package com.duyphuc.olympics.service;

import com.duyphuc.olympics.dao.MedalDAO;
import com.duyphuc.olympics.dao.OlympicEventDAO;
import com.duyphuc.olympics.db.DBConnectionManager;
import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;

import java.sql.Connection;
import java.sql.SQLException;
// ... other imports
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


public class MedalService {
    private final OlympicEventDAO olympicEventDAO;
    private final MedalDAO medalDAO;

    public MedalService() {
        this.olympicEventDAO = new OlympicEventDAO();
        this.medalDAO = new MedalDAO();
    }

    // ... other existing methods ...
    public List<OlympicEvent> getEvents() throws SQLException {
        return olympicEventDAO.getAllEvents();
    }

    public List<MedalEntry> getMedalDataForEvent(OlympicEvent event) throws SQLException {
        if (event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty()) {
            System.err.println("Invalid event or table name for fetching medal data.");
            return Collections.emptyList();
        }
        if ("PENDING_CREATION".equals(event.getTableNameInDb())) {
             System.err.println("Medal table for event '" + event.getEventName() + "' has not been properly created or linked.");
             return Collections.emptyList();
        }
        return medalDAO.getMedalsByEventTable(event.getTableNameInDb());
    }
    // ... (addMedal, updateMedal, deleteMedal for MedalEntry instances)
    public boolean addMedal(MedalEntry entry, OlympicEvent event) throws SQLException {
        if (entry == null || event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || "PENDING_CREATION".equals(event.getTableNameInDb())) {
            return false;
        }
        return medalDAO.addMedalEntry(entry, event.getTableNameInDb());
    }

    public boolean updateMedal(MedalEntry entry, OlympicEvent event) throws SQLException {
        if (entry == null || event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || entry.getId() <= 0 || "PENDING_CREATION".equals(event.getTableNameInDb())) {
            return false;
        }
        return medalDAO.updateMedalEntry(entry, event.getTableNameInDb());
    }

    public boolean deleteMedal(MedalEntry entry, OlympicEvent event) throws SQLException {
        if (entry == null || event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || entry.getId() <= 0 || "PENDING_CREATION".equals(event.getTableNameInDb())) {
            return false;
        }
        return medalDAO.deleteMedalEntry(entry.getId(), event.getTableNameInDb());
    }

    public List<String> getAllNOCsForEvent(OlympicEvent event) throws SQLException {
        if (event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || "PENDING_CREATION".equals(event.getTableNameInDb())) {
            return new ArrayList<>();
        }
        return medalDAO.getNOCsByEventTable(event.getTableNameInDb());
    }

    public List<MedalEntry> getTopNCountriesForEvent(OlympicEvent event, int N, String sortBy) throws SQLException {
        if (event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || "PENDING_CREATION".equals(event.getTableNameInDb())) {
            return new ArrayList<>();
        }
        List<MedalEntry> allMedals = medalDAO.getMedalsByEventTable(event.getTableNameInDb());
        // ... (comparator logic)
        Comparator<MedalEntry> comparator;
        switch (sortBy.toLowerCase()) {
            case "gold": comparator = Comparator.comparingInt(MedalEntry::getGold).reversed(); break;
            case "silver": comparator = Comparator.comparingInt(MedalEntry::getSilver).reversed(); break;
            case "bronze": comparator = Comparator.comparingInt(MedalEntry::getBronze).reversed(); break;
            default: comparator = Comparator.comparingInt(MedalEntry::getTotal).reversed(); break;
        }
        comparator = comparator
                        .thenComparing(Comparator.comparingInt(MedalEntry::getGold).reversed())
                        .thenComparing(Comparator.comparingInt(MedalEntry::getSilver).reversed())
                        .thenComparing(Comparator.comparingInt(MedalEntry::getBronze).reversed())
                        .thenComparing(MedalEntry::getNoc);
        return allMedals.stream().sorted(comparator).limit(N).collect(Collectors.toList());
    }

    public MedalEntry getMedalDataForCountryInEvent(OlympicEvent event, String NOC_Code) throws SQLException {
        if (event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || "PENDING_CREATION".equals(event.getTableNameInDb()) || NOC_Code == null || NOC_Code.trim().isEmpty()) return null;
        List<MedalEntry> allMedals = medalDAO.getMedalsByEventTable(event.getTableNameInDb());
        return allMedals.stream().filter(entry -> NOC_Code.equals(entry.getNoc())).findFirst().orElse(null);
    }

    public Map<Integer, Integer> getMedalTrendForCountry(String NOC_Code, List<OlympicEvent> allEvents, String medalType) throws SQLException {
        if (NOC_Code == null || NOC_Code.trim().isEmpty() || allEvents == null) return new HashMap<>();
        Map<Integer, Integer> trendData = new TreeMap<>();
        for (OlympicEvent event : allEvents) {
            if (event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || "PENDING_CREATION".equals(event.getTableNameInDb())) {
                continue;
            }
            MedalEntry countryDataInEvent = getMedalDataForCountryInEvent(event, NOC_Code);
            if (countryDataInEvent != null) {
                int count;
                switch (medalType.toLowerCase()) {
                    case "gold": count = countryDataInEvent.getGold(); break;
                    case "silver": count = countryDataInEvent.getSilver(); break;
                    case "bronze": count = countryDataInEvent.getBronze(); break;
                    default: count = countryDataInEvent.getTotal(); break;
                }
                if (count >= 0) { trendData.put(event.getYear(), count); }
            }
        }
        return trendData;
    }


    public OlympicEvent createOlympicEventWithTable(String eventName, int year, String eventType) throws SQLException {
        Connection conn = null;
        OlympicEvent newEvent = new OlympicEvent();
        newEvent.setEventName(eventName);
        newEvent.setYear(year);
        newEvent.setEventType(eventType);
        // table_name_in_db is not set on newEvent before calling addOlympicEvent

        try {
            conn = DBConnectionManager.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Add event details (without table_name_in_db) and get ID
            int eventId = olympicEventDAO.addOlympicEvent(newEvent, conn);
            newEvent.setId(eventId); // Set the ID on our Java object

            // Step 2: Generate the medal table name using the new eventId
            String actualTableName = "medals_event_" + eventId;
            newEvent.setTableNameInDb(actualTableName); // Set for the returned object and for the update

            // Step 3: Create the physical medal table
            medalDAO.createMedalTable(actualTableName, conn);

            // Step 4: Update the olympic_events record with the actual table name
            olympicEventDAO.updateOlympicEventTableName(eventId, actualTableName, conn);

            conn.commit(); // Commit transaction
            return newEvent;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction for event creation: " + ex.getMessage());
                }
            }
            // Re-throw with more context if desired, or let the original SQLException propagate
            throw new SQLException("Failed to create Olympic event and table: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Error closing connection after event creation: " + ex.getMessage());
                }
            }
        }
    }

    public void deleteOlympicEventWithTable(OlympicEvent event) throws SQLException {
        if (event == null || event.getId() <= 0 ) { // table_name_in_db can be null if it was never fully created
            throw new IllegalArgumentException("Invalid OlympicEvent object provided for deletion (ID missing).");
        }
        
        Connection conn = null;
        try {
            conn = DBConnectionManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Only drop table if a table name exists and is not a placeholder
            if (event.getTableNameInDb() != null && !event.getTableNameInDb().isEmpty() && !"PENDING_CREATION".equals(event.getTableNameInDb())) {
                medalDAO.dropMedalTable(event.getTableNameInDb(), conn);
            }
            olympicEventDAO.deleteOlympicEventById(event.getId(), conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw new SQLException("Failed to delete Olympic event and/or table: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}