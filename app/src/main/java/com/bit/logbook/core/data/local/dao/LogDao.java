package com.bit.logbook.core.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bit.logbook.core.data.local.entity.LogEntity;
import com.bit.logbook.core.data.local.entity.SyncStatus;

import java.util.List;

@Dao
public interface LogDao {

    @Query("SELECT * FROM logs WHERE isTrash = :isTrash ORDER BY startDate DESC")
    List<LogEntity> getAllLogs(boolean isTrash);

    @Query("SELECT * FROM logs WHERE isTrash = :isTrash AND startDate LIKE :datePrefix || '%' ORDER BY startDate DESC")
    List<LogEntity> getLogsByDate(String datePrefix, boolean isTrash);

    @Query("SELECT * FROM logs WHERE isTrash = :isTrash AND (:startDate IS NULL OR startDate LIKE :startDate || '%') ORDER BY startDate DESC")
    List<LogEntity> getLogsByOptionalDate(String startDate, boolean isTrash);

    @Query("SELECT * FROM logs WHERE id = :id")
    LogEntity getLogByServerId(String id);

    @Query("SELECT * FROM logs WHERE localId = :localId")
    LogEntity getLogByLocalId(long localId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LogEntity log);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<LogEntity> logs);

    @Update
    void update(LogEntity log);

    @Query("UPDATE logs SET syncStatus = :status, isTrash = :isTrash WHERE localId = :localId")
    void updateSyncStatus(long localId, SyncStatus status, boolean isTrash);

    @Query("UPDATE logs SET syncStatus = :status WHERE localId = :localId")
    void updateSyncStatusOnly(long localId, SyncStatus status);

    @Query("UPDATE logs SET id = :serverId, syncStatus = 'SYNCED' WHERE localId = :localId")
    void updateServerId(long localId, String serverId);

    @Query("DELETE FROM logs WHERE localId = :localId")
    void deleteByLocalId(long localId);

    @Query("DELETE FROM logs WHERE id IN (:ids)")
    void deleteByServerIds(List<String> ids);

    @Query("SELECT * FROM logs WHERE syncStatus != 'SYNCED' ORDER BY localId ASC")
    List<LogEntity> getPendingSyncLogs();

    @Query("SELECT COUNT(*) FROM logs WHERE syncStatus != 'SYNCED'")
    int getPendingSyncCount();

    @Query("DELETE FROM logs")
    void deleteAll();
}
