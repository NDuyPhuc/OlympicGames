package com.duyphuc.olympics.service;

import com.duyphuc.olympics.dao.MedalDAO;
import com.duyphuc.olympics.dao.OlympicEventDAO;
import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    // --- Các phương thức CRUD hiện có (điều chỉnh Exception Handling) ---
    public List<OlympicEvent> getEvents() throws SQLException { // Renamed from getAllEvents for consistency if needed
        return olympicEventDAO.getAllEvents();
    }

    public List<MedalEntry> getMedalDataForEvent(OlympicEvent event) throws SQLException {
        if (event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty()) {
            System.err.println("Invalid event or table name for fetching medal data.");
            return Collections.emptyList();
        }
        return medalDAO.getMedalsByEventTable(event.getTableNameInDb());
    }

    public boolean addMedal(MedalEntry entry, OlympicEvent event) throws SQLException {
        if (entry == null || event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty()) {
            return false;
        }
        entry.setTotal(entry.getGold() + entry.getSilver() + entry.getBronze());
        return medalDAO.addMedalEntry(entry, event.getTableNameInDb());
    }

    public boolean updateMedal(MedalEntry entry, OlympicEvent event) throws SQLException {
        if (entry == null || event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || entry.getId() <= 0) {
            return false;
        }
        entry.setTotal(entry.getGold() + entry.getSilver() + entry.getBronze());
        return medalDAO.updateMedalEntry(entry, event.getTableNameInDb());
    }

    public boolean deleteMedal(MedalEntry entry, OlympicEvent event) throws SQLException {
        if (entry == null || event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty() || entry.getId() <= 0) {
            return false;
        }
        return medalDAO.deleteMedalEntry(entry.getId(), event.getTableNameInDb());
    }

    // --- Các phương thức mới cho ChartService ---

    public List<String> getAllNOCsForEvent(OlympicEvent event) throws SQLException {
        if (event == null || event.getTableNameInDb() == null || event.getTableNameInDb().isEmpty()) {
            return new ArrayList<>();
        }
        return medalDAO.getNOCsByEventTable(event.getTableNameInDb());
    }

    public List<MedalEntry> getTopNCountriesForEvent(OlympicEvent event, int N, String sortBy) throws SQLException {
        if (event == null) return new ArrayList<>();
        List<MedalEntry> allMedals = medalDAO.getMedalsByEventTable(event.getTableNameInDb());

        Comparator<MedalEntry> comparator;
        switch (sortBy.toLowerCase()) {
            case "gold":
                comparator = Comparator.comparingInt(MedalEntry::getGold).reversed();
                break;
            case "silver":
                comparator = Comparator.comparingInt(MedalEntry::getSilver).reversed();
                break;
            case "bronze":
                comparator = Comparator.comparingInt(MedalEntry::getBronze).reversed();
                break;
            default: // "total" hoặc mặc định
                comparator = Comparator.comparingInt(MedalEntry::getTotal).reversed();
                break;
        }
        comparator = comparator
                        .thenComparing(Comparator.comparingInt(MedalEntry::getGold).reversed())
                        .thenComparing(Comparator.comparingInt(MedalEntry::getSilver).reversed())
                        .thenComparing(Comparator.comparingInt(MedalEntry::getBronze).reversed())
                        .thenComparing(MedalEntry::getNoc); // <<< FIX: Changed from getNOC to getNoc (line 117 related error)


        return allMedals.stream()
                        .sorted(comparator)
                        .limit(N)
                        .collect(Collectors.toList());
    }

    public MedalEntry getMedalDataForCountryInEvent(OlympicEvent event, String NOC_Code) throws SQLException { // Renamed NOC to NOC_Code to avoid confusion with field name
        if (event == null || NOC_Code == null || NOC_Code.trim().isEmpty()) return null;
        List<MedalEntry> allMedals = medalDAO.getMedalsByEventTable(event.getTableNameInDb());
        return allMedals.stream()
                .filter(entry -> NOC_Code.equals(entry.getNoc())) // <<< FIX: Changed from getNOC to getNoc (line 140 related error)
                .findFirst()
                .orElse(null);
    }

    public Map<Integer, Integer> getMedalTrendForCountry(String NOC_Code, List<OlympicEvent> allEvents, String medalType) throws SQLException { // Renamed NOC
        if (NOC_Code == null || NOC_Code.trim().isEmpty() || allEvents == null) return new HashMap<>();
        
        Map<Integer, Integer> trendData = new TreeMap<>(); 
        for (OlympicEvent event : allEvents) {
            MedalEntry countryDataInEvent = getMedalDataForCountryInEvent(event, NOC_Code); // Pass NOC_Code
            
            if (countryDataInEvent != null) {
                int count;
                switch (medalType.toLowerCase()) {
                    case "gold": count = countryDataInEvent.getGold(); break;
                    case "silver": count = countryDataInEvent.getSilver(); break;
                    case "bronze": count = countryDataInEvent.getBronze(); break;
                    default: count = countryDataInEvent.getTotal(); break;
                }
                if (count >= 0) {
                     trendData.put(event.getYear(), count);
                }
            }
        }
        return trendData;
    }
}