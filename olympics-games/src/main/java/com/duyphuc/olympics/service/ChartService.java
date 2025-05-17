package com.duyphuc.olympics.service;

import com.duyphuc.olympics.model.MedalEntry;
import com.duyphuc.olympics.model.OlympicEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot; // Giữ import này
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Map;

public class ChartService implements IChartService {

    @Override
    public ChartPanel createTopNCountriesBarChart(List<MedalEntry> topNMedals, OlympicEvent event, int N, String sortBy) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Đảm bảo sortBy không null để tránh NullPointerException
        String sortCriteria = (sortBy == null) ? "Total" : sortBy;
        String seriesLabel = sortCriteria.substring(0, 1).toUpperCase() + sortCriteria.substring(1).toLowerCase() + " Medals";

        if (topNMedals == null || topNMedals.isEmpty()) {
            // Trả về biểu đồ rỗng với thông báo nếu không có dữ liệu
            JFreeChart emptyChart = ChartFactory.createBarChart(
                "Top " + N + " Countries - No Data Available",
                "Country (NOC)", "Number of Medals", dataset,
                PlotOrientation.VERTICAL, false, false, false);
            return new ChartPanel(emptyChart);
        }

        for (MedalEntry entry : topNMedals) {
            int value;
            switch (sortCriteria.toLowerCase()) {
                case "gold": value = entry.getGold(); break;
                case "silver": value = entry.getSilver(); break;
                case "bronze": value = entry.getBronze(); break;
                default: value = entry.getTotal(); break; // Mặc định là Total
            }
            dataset.addValue(value, seriesLabel, entry.getNoc()); // Sử dụng getNoc()
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Top " + N + " Countries by " + seriesLabel + " - " + event.getEventName(),
                "Country (NOC)",
                "Number of Medals",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        
        CategoryPlot plot = barChart.getCategoryPlot();
        if (plot != null) { // Kiểm tra plot không null
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(79, 129, 189));
            plot.setBackgroundPaint(Color.white);

            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }

        return new ChartPanel(barChart);
    }

    @Override
    public ChartPanel createMedalDistributionPieChart(MedalEntry countryMedals, OlympicEvent event) {
        String eventName = (event != null && event.getEventName() != null) ? event.getEventName() : "Unknown Event";
        String countryNoc = (countryMedals != null && countryMedals.getNoc() != null) ? countryMedals.getNoc() : "N/A";

        if (countryMedals == null || (countryMedals.getGold() == 0 && countryMedals.getSilver() == 0 && countryMedals.getBronze() == 0)) {
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            dataset.setValue("No Medal Data", 100);
             JFreeChart pieChart = ChartFactory.createPieChart(
                "Medal Distribution for " + countryNoc + " - " + eventName,
                dataset,
                true, true, false);
            @SuppressWarnings("unchecked")
			PiePlot<String> plot = (PiePlot<String>) pieChart.getPlot();
            if (plot != null) {
                plot.setNoDataMessage("No medal data available for this selection.");
                 plot.setSectionPaint("No Medal Data", Color.LIGHT_GRAY);
            }
            return new ChartPanel(pieChart);
        }

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        if (countryMedals.getGold() > 0) dataset.setValue("Gold (" + countryMedals.getGold() + ")", countryMedals.getGold());
        if (countryMedals.getSilver() > 0) dataset.setValue("Silver (" + countryMedals.getSilver() + ")", countryMedals.getSilver());
        if (countryMedals.getBronze() > 0) dataset.setValue("Bronze (" + countryMedals.getBronze() + ")", countryMedals.getBronze());

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Medal Distribution for " + countryNoc + " - " + eventName,
                dataset,
                true, // legend
                true, // tooltips
                false // urls
        );

        @SuppressWarnings("unchecked")
		PiePlot<String> plot = (PiePlot<String>) pieChart.getPlot(); // Sửa lỗi raw type
        if (plot != null) { // Kiểm tra plot không null
            if (countryMedals.getGold() > 0) plot.setSectionPaint("Gold (" + countryMedals.getGold() + ")", new Color(255, 215, 0));
            if (countryMedals.getSilver() > 0) plot.setSectionPaint("Silver (" + countryMedals.getSilver() + ")", new Color(192, 192, 192));
            if (countryMedals.getBronze() > 0) plot.setSectionPaint("Bronze (" + countryMedals.getBronze() + ")", new Color(205, 127, 50));
            plot.setBackgroundPaint(Color.white);
            plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
            plot.setNoDataMessage("No data to display");
            plot.setCircular(true);
            // plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1} ({2})")); // Ví dụ format label
        }
        return new ChartPanel(pieChart);
    }

    @Override
    public ChartPanel createCountryTrendLineChart(Map<Integer, Integer> medalTrendData, String NOC, String yAxisLabel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String countryCode = (NOC == null) ? "Unknown Country" : NOC;
        String yLabel = (yAxisLabel == null) ? "Number of Medals" : yAxisLabel;

        if (medalTrendData == null || medalTrendData.isEmpty()) {
            // Trả về biểu đồ rỗng với thông báo nếu không có dữ liệu
             JFreeChart emptyChart = ChartFactory.createLineChart(
                "Medal Trend for " + countryCode + " - No Data Available",
                "Olympic Year", yLabel, dataset,
                PlotOrientation.VERTICAL, false, false, false);
            return new ChartPanel(emptyChart);
        }

        for (Map.Entry<Integer, Integer> entry : medalTrendData.entrySet()) {
            dataset.addValue(entry.getValue(), countryCode, String.valueOf(entry.getKey()));
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Medal Trend for " + countryCode + " (" + yLabel + ")",
                "Olympic Year",
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                false, // Tắt legend nếu chỉ có 1 series (NOC)
                true,  // tooltips
                false  // urls
        );

        CategoryPlot plot = lineChart.getCategoryPlot();
        if (plot != null) { // Kiểm tra plot không null
            plot.setBackgroundPaint(Color.white);
            plot.setDomainGridlinePaint(Color.lightGray);
            plot.setRangeGridlinePaint(Color.lightGray);
            
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        return new ChartPanel(lineChart);
    }
}