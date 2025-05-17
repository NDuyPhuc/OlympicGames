package com.duyphuc.olympics.service;

import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IReportService {
    List<MedalEntry> generateOverallRankingReportForEvent(OlympicEvent event) throws SQLException;
    Optional<MedalEntry> getCountryWithMostGoldMedalsInEvent(OlympicEvent event) throws SQLException;
    Map<String, Integer> getTotalMedalsAwardedInEvent(OlympicEvent event) throws SQLException;
    List<MedalEntry> getTopNCountriesByTotalMedalsInEvent(OlympicEvent event, int n) throws SQLException;
    List<MedalEntry> getCountryPerformanceAcrossEvents(String noc) throws SQLException;
    List<MedalEntry> getOverallLeaderboardAllEvents(int topN) throws SQLException;
}