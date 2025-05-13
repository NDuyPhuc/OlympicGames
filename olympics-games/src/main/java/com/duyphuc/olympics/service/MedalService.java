package com.duyphuc.olympics.service;

import com.duyphuc.olympics.dao.MedalDAO;
import com.duyphuc.olympics.dao.OlympicEventDAO;
import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;
import java.util.Collections;
import java.util.List;

public class MedalService {
    private final OlympicEventDAO olympicEventDAO;
    private final MedalDAO medalDAO;

    public MedalService() {
        this.olympicEventDAO = new OlympicEventDAO();
        this.medalDAO = new MedalDAO();
    }

    public List<OlympicEvent> getEvents() {
        try {
            return olympicEventDAO.getAllEvents();
        } catch (Exception e) {
            System.err.println("Service error fetching events: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<MedalEntry> getMedalDataForEvent(OlympicEvent event) {
        if (event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty()) {
            System.err.println("Invalid event or table name for fetching medal data.");
            return Collections.emptyList();
        }
        try {
            return medalDAO.getMedalsByEventTable(event.getTableNameInDb());
        } catch (Exception e) {
            System.err.println("Service error fetching medal data for event " + event.getEventName() + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean addMedal(MedalEntry entry, OlympicEvent event) {
        if (entry == null || event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty()) {
            System.err.println("Invalid entry or event for adding medal.");
            return false;
        }
        // Đảm bảo Total được tính toán chính xác trước khi lưu
        entry.setTotal(entry.getGold() + entry.getSilver() + entry.getBronze());
        try {
            return medalDAO.addMedalEntry(entry, event.getTableNameInDb());
        } catch (Exception e) {
            System.err.println("Service error adding medal: " + e.getMessage());
            return false;
        }
    }

    public boolean updateMedal(MedalEntry entry, OlympicEvent event) {
        if (entry == null || event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || entry.getId() <= 0) {
            System.err.println("Invalid entry, event, or entry ID for updating medal.");
            return false;
        }
        entry.setTotal(entry.getGold() + entry.getSilver() + entry.getBronze());
         try {
            return medalDAO.updateMedalEntry(entry, event.getTableNameInDb());
        } catch (Exception e) {
            System.err.println("Service error updating medal: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMedal(MedalEntry entry, OlympicEvent event) {
        if (entry == null || event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || entry.getId() <= 0) {
            System.err.println("Invalid entry, event, or entry ID for deleting medal.");
            return false;
        }
        try {
            return medalDAO.deleteMedalEntry(entry.getId(), event.getTableNameInDb());
        } catch (Exception e) {
            System.err.println("Service error deleting medal: " + e.getMessage());
            return false;
        }
    }
}
