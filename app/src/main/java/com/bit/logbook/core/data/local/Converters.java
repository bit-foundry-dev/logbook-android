package com.bit.logbook.core.data.local;

import androidx.room.TypeConverter;

import com.bit.logbook.core.data.local.entity.SyncStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Converters {

    @TypeConverter
    public static LocalDateTime fromString(String value) {
        return value == null ? null : LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
    }

    @TypeConverter
    public static String fromLocalDateTime(LocalDateTime date) {
        return date == null ? null : date.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    @TypeConverter
    public static SyncStatus fromSyncStatusString(String value) {
        return value == null ? SyncStatus.SYNCED : SyncStatus.valueOf(value);
    }

    @TypeConverter
    public static String fromSyncStatus(SyncStatus status) {
        return status == null ? SyncStatus.SYNCED.name() : status.name();
    }
}
