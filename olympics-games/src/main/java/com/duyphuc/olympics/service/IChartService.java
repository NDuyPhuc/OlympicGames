package com.duyphuc.olympics.service;

import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;
import org.jfree.chart.ChartPanel;
import java.util.List;
import java.util.Map;

public interface IChartService {
    ChartPanel createTopNCountriesBarChart(List<MedalEntry> topNMedals, OlympicEvent event, int N, String sortBy);
    ChartPanel createMedalDistributionPieChart(MedalEntry countryMedals, OlympicEvent event);
    ChartPanel createCountryTrendLineChart(Map<Integer, Integer> medalTrendData, String NOC, String yAxisLabel);
}