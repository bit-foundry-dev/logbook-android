package com.bit.logbook.feature.logManagement.domain.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private final String id;
    private final String title;
    private final String description;
    private final LocalDateTime startDate;
    private final String tag;

    public Log(String id, String title, String description, String tag,
               LocalDateTime startDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tag = tag;
        this.startDate = startDate;
    }

    public Log(String id, String title, String description, String tag,
               String startDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tag = tag;
        this.startDate = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public String getTag() {
        return tag;
    }
}