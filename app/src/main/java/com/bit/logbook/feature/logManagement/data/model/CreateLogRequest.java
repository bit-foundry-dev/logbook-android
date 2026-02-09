package com.bit.logbook.feature.logManagement.data.model;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateLogRequest {
    private String title;
    private String description;
    private String tag;
    private String startDate;

    public CreateLogRequest(String title, String description, String tag, String startDate) {
        this.title = title;
        this.description = description;
        this.tag = tag;
        this.startDate = startDate;
    }

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
}
