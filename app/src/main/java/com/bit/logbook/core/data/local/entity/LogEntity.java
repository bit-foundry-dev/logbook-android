package com.bit.logbook.core.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "logs")
public class LogEntity {
    @PrimaryKey(autoGenerate = true)
    private long localId;
    private String id;
    private String title;
    private String description;
    private String tag;
    private String startDate;
    private boolean isTrash;
    private SyncStatus syncStatus;

    public LogEntity(String id, String title, String description, String tag,
                     String startDate, boolean isTrash, SyncStatus syncStatus) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tag = tag;
        this.startDate = startDate;
        this.isTrash = isTrash;
        this.syncStatus = syncStatus;
    }

    public long getLocalId() { return localId; }
    public void setLocalId(long localId) { this.localId = localId; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public boolean isTrash() { return isTrash; }
    public void setTrash(boolean trash) { isTrash = trash; }
    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
}
