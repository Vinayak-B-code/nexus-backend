package com.nexus.dto;

import java.util.ArrayList;
import java.util.List;

public class ActivityBreakdownDto {

    private String range;
    private long total;
    private List<CategoryItem> items = new ArrayList<>();

    public static class CategoryItem {
        private String category;
        private long count;
        private double percentage;
        private String color;

        public CategoryItem() {}

        public CategoryItem(String category, long count, double percentage, String color) {
            this.category = category;
            this.count = count;
            this.percentage = percentage;
            this.color = color;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public ActivityBreakdownDto() {}

    public ActivityBreakdownDto(String range, long total, List<CategoryItem> items) {
        this.range = range;
        this.total = total;
        this.items = items;
    }

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public List<CategoryItem> getItems() { return items; }
    public void setItems(List<CategoryItem> items) { this.items = items; }
}
