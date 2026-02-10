package com.bit.logbook.feature.logManagement.data.model;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpdateLogRequest {
    private String title;
    private String description;
    private String tag;
    private String startDate;
    private boolean isTrash;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description.isEmpty() || description.isBlank() ? null : description;
    }

    public String getTag() {
        return tag.isEmpty() || tag.isBlank() ? null : tag;
    }

    public String getStartDate() {
        return startDate == null ? LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : startDate;
    }

    public boolean isTrash() {
        return isTrash;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setTrash(boolean trash) {
        isTrash = trash;
    }
}
