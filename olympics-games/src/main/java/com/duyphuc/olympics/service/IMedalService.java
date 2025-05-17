package com.duyphuc.olympics.service;

import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IMedalService {
    List<OlympicEvent> getEvents() throws SQLException;
    List<MedalEntry> getMedalDataForEvent(OlympicEvent event) throws SQLException;
    boolean addMedal(MedalEntry entry, OlympicEvent event) throws SQLException;
    boolean updateMedal(MedalEntry entry, OlympicEvent event) throws SQLException;
    boolean deleteMedal(MedalEntry entry, OlympicEvent event) throws SQLException;
    List<String> getAllNOCsForEvent(OlympicEvent event) throws SQLException;
    List<MedalEntry> getTopNCountriesForEvent(OlympicEvent event, int N, String sortBy) throws SQLException;
    MedalEntry getMedalDataForCountryInEvent(OlympicEvent event, String NOC_Code) throws SQLException;
    Map<Integer, Integer> getMedalTrendForCountry(String NOC_Code, List<OlympicEvent> allEvents, String medalType) throws SQLException;
    OlympicEvent createOlympicEventWithTable(String eventName, int year, String eventType) throws SQLException;
    void deleteOlympicEventWithTable(OlympicEvent event) throws SQLException;
}