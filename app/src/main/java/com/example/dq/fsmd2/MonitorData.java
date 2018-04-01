package com.example.dq.fsmd2;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by DQ on 12/16/2017.
 */

public class MonitorData implements Serializable
{
    private VibrationData vibData;
    private PositionData posData;
    private TimeData timeData;

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

    // TODO: Implement copy constructor
    /*
    public MonitorData(MonitorData other) {

    }
    */

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
}
