package com.example.dq.fsmd2;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by DQ on 12/16/2017.
 */

@Entity(foreignKeys = @ForeignKey(entity = Item.class, parentColumns = "itemID", childColumns = "itemID"))
@TypeConverters({VibrationDataConverter.class, PositionDataConverter.class, DateConverter.class})
public class MonitorData implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private byte monitorDataID;

    private VibrationData vibData;
    private PositionData posData;
    private Date timeData;

    private byte itemID;

    @Ignore
    private boolean isViewExpanded;

    public MonitorData() {
        vibData = null;
        posData = null;
        timeData = null;
        isViewExpanded = false;
    }

    public MonitorData(byte itemID, float peakXVib, float peakYVib, float peakZVib,
                       float avgXVib, float avgYVib, float avgZVib,
                       float longitude, float latitude,
                       Date date) {
        this.itemID = itemID;
        vibData = new VibrationData(peakXVib, peakYVib, peakZVib, avgXVib, avgYVib, avgZVib);
        posData = new PositionData(longitude, latitude);
        timeData = date;
        isViewExpanded = false;
    }

    public byte getMonitorDataID() {
        return monitorDataID;
    }
    public void setMonitorDataID(byte monitorDataID) {
        this.monitorDataID = monitorDataID;
    }

    public void setVibData(VibrationData vibData) {
        this.vibData = vibData;
    }

    public void setPosData(PositionData posData) {
        this.posData = posData;
    }

    public void setVibData(float peakXVib, float peakYVib, float peakZVib,
                                 float avgXVib, float avgYVib, float avgZVib) {
        vibData.setPeakXVibration(peakXVib);
        vibData.setPeakYVibration(peakYVib);
        vibData.setPeakZVibration(peakZVib);

        vibData.setAvgXVibration(avgXVib);
        vibData.setAvgYVibration(avgYVib);
        vibData.setAvgZVibration(avgZVib);
    }

    public VibrationData getVibData() { return vibData; }
    public void setPosData(float longitude, float latitude) {
        posData.setLongitude(longitude);
        posData.setLatitude(latitude);
    }

    public PositionData getPosData() { return posData; }

    public void setTimeData(Date date) {
        this.timeData = date;
    }
    public Date getTimeData() { return timeData; }

    public byte getItemID() {
        return itemID;
    }
    public void setItemID(byte itemID) {
        this.itemID = itemID;
    }

    public boolean isViewExpanded() {
        return isViewExpanded;
    }

    public void setViewExpanded(boolean viewExpanded) {
        isViewExpanded = viewExpanded;
    }

    @Dao
    public interface MonitorDataDao {
        @Insert
        void insertAll(MonitorData... monitorDatas);

        @Delete
        void delete(MonitorData monitorData);

        @Query("DELETE FROM monitorData WHERE itemID is :itemID")
        void deleteMonitorDatasFromItem(int itemID);

        @Query("SELECT * FROM monitordata ORDER BY timeData DESC")
        LiveData<List<MonitorData>> getAllMonitorDatas();

        @Query("SELECT COUNT(*) from monitordata WHERE itemID IS :itemID")
        int getNumMonitorDatasFromItem(int itemID);

        @Query("SELECT * FROM monitordata WHERE itemID IS :itemID ORDER BY timeData DESC")
        List<MonitorData> getAllCurrentMonitorDatasFromItem(int itemID);

        @Query("SELECT * FROM monitordata WHERE itemID IS :itemID ORDER BY timeData DESC")
        LiveData<List<MonitorData>> getAllMonitorDatasFromItem(int itemID);

        @Query("SELECT MAX(timeData) FROM monitordata WHERE itemID IS :itemID")
        long getLatestTimestamp(int itemID);
    }
}

class DateConverter {

    @TypeConverter
    public static Date toDate(long milliseconds) {
        return new Date(milliseconds);
    }

    @TypeConverter
    public static long toLong(Date date) {
        return date.getTime();
    }

}