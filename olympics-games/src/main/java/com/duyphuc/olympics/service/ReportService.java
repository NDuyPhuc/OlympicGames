package com.duyphuc.olympics.service;

import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;

import java.sql.SQLException; // Thêm import này
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReportService is responsible for generating various analytical reports
 * based on Olympic medal data. It utilizes MedalService to fetch the
 * necessary data.
 */
public class ReportService {

    private final MedalService medalService;

    public ReportService(MedalService medalService) {
        this.medalService = medalService;
    }

    /**
     * Generates an overall ranking report for a specific Olympic event.
     * Ranking is based on:
     * 1. Most Gold medals
     * 2. Most Silver medals (if Gold medals are tied)
     * 3. Most Bronze medals (if Gold and Silver medals are tied)
     * 4. Alphabetical by NOC (if all medals are tied)
     *
     * @param event The Olympic event to generate the report for.
     * @return A list of MedalEntry objects, sorted according to Olympic ranking criteria.
     *         Returns an empty list if no data is available for the event.
     * @throws SQLException if a database access error occurs.
     */
    public List<MedalEntry> generateOverallRankingReportForEvent(OlympicEvent event) throws SQLException {
        if (event == null) { // Chỉ cần kiểm tra event, MedalService sẽ xử lý event.getTableNameInDb()
            System.err.println("ReportService: Invalid OlympicEvent provided for ranking report.");
            return Collections.emptyList();
        }
        // SỬA LỖI: Gọi đúng phương thức của MedalService
        List<MedalEntry> medalEntries = medalService.getMedalDataForEvent(event);
        if (medalEntries == null) { // MedalService có thể trả về null hoặc list rỗng, xử lý cả hai
             System.err.println("ReportService: No medal data found for event: " + event.getEventName());
            return Collections.emptyList();
        }

        return medalEntries.stream()
                .sorted(
                    Comparator.comparingInt(MedalEntry::getGold).reversed()
                    .thenComparingInt(MedalEntry::getSilver).reversed()
                    .thenComparingInt(MedalEntry::getBronze).reversed()
                    .thenComparing(MedalEntry::getNoc)
                )
                .collect(Collectors.toList());
    }

    /**
     * Finds the country (NOC) with the most gold medals in a specific Olympic event.
     *
     * @param event The Olympic event.
     * @return An Optional containing the MedalEntry of the country with the most gold medals,
     *         or Optional.empty() if no data or no gold medals were awarded.
     * @throws SQLException if a database access error occurs.
     */
    public Optional<MedalEntry> getCountryWithMostGoldMedalsInEvent(OlympicEvent event) throws SQLException {
        if (event == null) {
            return Optional.empty();
        }
        // SỬA LỖI: Gọi đúng phương thức
        List<MedalEntry> medalEntries = medalService.getMedalDataForEvent(event);
        if (medalEntries == null || medalEntries.isEmpty()) {
            return Optional.empty();
        }

        return medalEntries.stream()
                .filter(entry -> entry.getGold() > 0)
                .max(Comparator.comparingInt(MedalEntry::getGold));
    }

    /**
     * Calculates the total number of gold, silver, bronze, and overall medals awarded
     * in a specific Olympic event.
     *
     * @param event The Olympic event.
     * @return A Map where keys are "Gold", "Silver", "Bronze", "Total" and values are their counts.
     *         Returns an empty map if no data is available.
     * @throws SQLException if a database access error occurs.
     */
    public Map<String, Integer> getTotalMedalsAwardedInEvent(OlympicEvent event) throws SQLException {
        Map<String, Integer> totals = new HashMap<>();
        totals.put("Gold", 0);
        totals.put("Silver", 0);
        totals.put("Bronze", 0);
        totals.put("Total", 0);

        if (event == null) {
            return totals; // Return zeroed map
        }

        // SỬA LỖI: Gọi đúng phương thức
        List<MedalEntry> medalEntries = medalService.getMedalDataForEvent(event);
        if (medalEntries == null || medalEntries.isEmpty()) {
            return totals; // Return zeroed map
        }

        int totalGold = medalEntries.stream().mapToInt(MedalEntry::getGold).sum();
        int totalSilver = medalEntries.stream().mapToInt(MedalEntry::getSilver).sum();
        int totalBronze = medalEntries.stream().mapToInt(MedalEntry::getBronze).sum();

        totals.put("Gold", totalGold);
        totals.put("Silver", totalSilver);
        totals.put("Bronze", totalBronze);
        totals.put("Total", totalGold + totalSilver + totalBronze);

        return totals;
    }

    /**
     * Generates a report of the top N countries by total medals in a specific Olympic event.
     *
     * @param event The Olympic event.
     * @param n     The number of top countries to return.
     * @return A list of MedalEntry objects for the top N countries, sorted by total medals descending.
     *         Returns an empty list if no data or n is invalid.
     * @throws SQLException if a database access error occurs.
     */
    public List<MedalEntry> getTopNCountriesByTotalMedalsInEvent(OlympicEvent event, int n) throws SQLException {
        if (event == null || n <= 0) {
            return Collections.emptyList();
        }
        // SỬA LỖI: Gọi đúng phương thức
        List<MedalEntry> medalEntries = medalService.getMedalDataForEvent(event);
         if (medalEntries == null) {
            return Collections.emptyList();
        }

        return medalEntries.stream()
                .sorted(Comparator.comparingInt(MedalEntry::getTotal).reversed()
                                  .thenComparing(MedalEntry::getNoc)) // Alphabetical for ties in total
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Generates a summary of a specific country's performance across all Olympic events
     * for which data is available.
     *
     * @param noc The National Olympic Committee code (e.g., "USA").
     * @return A list of MedalEntry objects, one for each event the country participated in.
     * @throws SQLException if a database access error occurs.
     */
    public List<MedalEntry> getCountryPerformanceAcrossEvents(String noc) throws SQLException {
        if (noc == null || noc.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // SỬA LỖI: Gọi đúng phương thức của MedalService
        List<OlympicEvent> allEvents = medalService.getEvents(); // Đổi từ getAllOlympicEvents()
        List<MedalEntry> countryPerformance = new ArrayList<>();

        if (allEvents == null) return Collections.emptyList(); // Thêm kiểm tra null cho allEvents

        for (OlympicEvent event : allEvents) {
            // SỬA LỖI: Gọi đúng phương thức
            List<MedalEntry> eventMedals = medalService.getMedalDataForEvent(event);
            if (eventMedals != null) {
                eventMedals.stream()
                    .filter(entry -> noc.equals(entry.getNoc()))
                    .findFirst()
                    .ifPresent(medalEntry -> {
                        MedalEntry performanceEntry = new MedalEntry(
                            medalEntry.getNoc(),
                            medalEntry.getGold(),
                            medalEntry.getSilver(),
                            medalEntry.getBronze()
                        );
                        performanceEntry.setOlympicEventYear(event.getYear());
                        performanceEntry.setOlympicEventType(event.getEventType());
                        countryPerformance.add(performanceEntry);
                    });
            }
        }
        countryPerformance.sort(Comparator.comparingInt(MedalEntry::getOlympicEventYear));
        return countryPerformance;
    }


    /**
     * Generates an overall leaderboard across all available Olympic events.
     *
     * @param topN The number of top countries to include in the leaderboard.
     * @return A list of MedalEntry objects representing the aggregated performance of top N countries.
     * @throws SQLException if a database access error occurs.
     */
    public List<MedalEntry> getOverallLeaderboardAllEvents(int topN) throws SQLException {
        if (topN <= 0) {
            return Collections.emptyList();
        }

        // SỬA LỖI: Gọi đúng phương thức
        List<OlympicEvent> allEvents = medalService.getEvents(); // Đổi từ getAllOlympicEvents()
        if (allEvents == null || allEvents.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, MedalCountAggregator> aggregatedMedals = new HashMap<>();

        for (OlympicEvent event : allEvents) {
            // SỬA LỖI: Gọi đúng phương thức
            List<MedalEntry> eventMedals = medalService.getMedalDataForEvent(event);
            if (eventMedals != null) {
                for (MedalEntry entry : eventMedals) {
                    aggregatedMedals.computeIfAbsent(entry.getNoc(), k -> new MedalCountAggregator(k))
                                    .addMedals(entry.getGold(), entry.getSilver(), entry.getBronze());
                }
            }
        }

        return aggregatedMedals.values().stream()
                .map(aggregator -> new MedalEntry(aggregator.getNoc(), aggregator.getGold(), aggregator.getSilver(), aggregator.getBronze()))
                .sorted(
                    Comparator.comparingInt(MedalEntry::getGold).reversed()
                    .thenComparingInt(MedalEntry::getSilver).reversed()
                    .thenComparingInt(MedalEntry::getBronze).reversed()
                    .thenComparing(MedalEntry::getNoc)
                )
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Helper class to accumulate medal counts for a NOC across multiple events.
     */
    private static class MedalCountAggregator {
        private final String noc;
        private int gold;
        private int silver;
        private int bronze;

        public MedalCountAggregator(String noc) {
            this.noc = noc;
            this.gold = 0;
            this.silver = 0;
            this.bronze = 0;
        }

        public void addMedals(int gold, int silver, int bronze) {
            this.gold += gold;
            this.silver += silver;
            this.bronze += bronze;
        }

        public String getNoc() { return noc; }
        public int getGold() { return gold; }
        public int getSilver() { return silver; }
        public int getBronze() { return bronze; }
    }
}