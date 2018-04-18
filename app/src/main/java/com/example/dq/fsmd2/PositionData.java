package com.example.dq.fsmd2;

import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Scanner;

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

class PositionDataConverter {

    @TypeConverter
    public static PositionData toPositionData(String value) {
        Scanner sc = new Scanner(value);
        sc.useDelimiter("\t");
        double latitude = sc.nextDouble();
        double longitude = sc.nextDouble();
        return new PositionData(latitude, longitude);
    }

    @TypeConverter
    public static String toString(PositionData positionData) {
        return String.valueOf(positionData.getLatitude()) + "\t" + String.valueOf(positionData.getLongitude());
    }

}