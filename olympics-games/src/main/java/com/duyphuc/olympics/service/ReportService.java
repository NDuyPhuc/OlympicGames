package com.duyphuc.olympics.service;

import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;

import java.sql.SQLException; 
import java.util.*;
import java.util.stream.Collectors;


public class ReportService implements IReportService {

    private final IMedalService medalService;

    public ReportService(IMedalService medalService) {
        this.medalService = medalService;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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


    @Override
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