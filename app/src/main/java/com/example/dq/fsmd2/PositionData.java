package com.example.dq.fsmd2;

/**
 * Created by DQ on 12/16/2017.
 */

public class PositionData {
    private double longitude;
    private double latitude;

    public PositionData() {
        longitude = 0;
        latitude = 0;
    }

    public PositionData(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public void setLongitude(double date) { this.longitude = longitude; }
    public double getLongitude() { return longitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLatitude() { return latitude; }
}
