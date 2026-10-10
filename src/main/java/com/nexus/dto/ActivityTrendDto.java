package com.nexus.dto;

import java.util.ArrayList;
import java.util.List;

public class ActivityTrendDto {

    private String range;
    private List<String> labels = new ArrayList<>();
    private List<String> dates = new ArrayList<>();
    private List<Long> dataPoints = new ArrayList<>();
    private long totalInRange;

    public ActivityTrendDto() {}

    public ActivityTrendDto(String range, List<String> labels, List<String> dates, List<Long> dataPoints, long totalInRange) {
        this.range = range;
        this.labels = labels;
        this.dates = dates;
        this.dataPoints = dataPoints;
        this.totalInRange = totalInRange;
    }

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }

    public List<Long> getDataPoints() { return dataPoints; }
    public void setDataPoints(List<Long> dataPoints) { this.dataPoints = dataPoints; }

    public long getTotalInRange() { return totalInRange; }
    public void setTotalInRange(long totalInRange) { this.totalInRange = totalInRange; }
}
