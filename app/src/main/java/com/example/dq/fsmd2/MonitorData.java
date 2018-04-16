package com.example.dq.fsmd2;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by DQ on 12/16/2017.
 */

@Entity(foreignKeys = @ForeignKey(entity = Item.class, parentColumns = "itemID", childColumns = "itemID"))
public class MonitorData implements Serializable
{
    private VibrationData vibData;
    private PositionData posData;
    private TimeData timeData;

    private int itemID;

    public MonitorData() {
        vibData = null;
        posData = null;
        timeData = null;
    }

    public MonitorData(double peakXVib, double peakYVib, double peakZVib,
                       double avgXVib, double avgYVib, double avgZVib,
                       double longitude, double latitude,
                       Date date) {
        vibData = new VibrationData(peakXVib, peakYVib, peakZVib, avgXVib, avgYVib, avgZVib);
        posData = new PositionData(longitude, latitude);
        timeData = new TimeData(date);
    }

    public void setVibrationData(double peakXVib, double peakYVib, double peakZVib,
                                 double avgXVib, double avgYVib, double avgZVib) {
        vibData.setPeakXVibration(peakXVib);
        vibData.setPeakYVibration(peakYVib);
        vibData.setPeakZVibration(peakZVib);

        vibData.setAvgXVibration(avgXVib);
        vibData.setAvgYVibration(avgYVib);
        vibData.setAvgZVibration(avgZVib);
    }

    public VibrationData getVibrationData() { return vibData; }
    public void setPositionData(double longitude, double latitude) {
        posData.setLongitude(longitude);
        posData.setLatitude(latitude);
    }

    public PositionData getPositionData() { return posData; }

    public void setTimeData(Date date) {
        timeData.setDate(date);
    }
    public TimeData getTimeData() { return timeData; }

    @Dao
    public interface MonitorDataDao {
        @Insert
        void insertAll(MonitorData... monitorDatas);

        @Delete
        void delete(MonitorData monitorData);

        @Query("SELECT * FROM monitordata WHERE itemID IS :itemID")
        LiveData<List<MonitorData>> getAllMonitorDataFromItem(int itemID);
    }
}
