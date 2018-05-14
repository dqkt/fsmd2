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
    private float longitude;
    private float latitude;

    public PositionData() {
        longitude = 0;
        latitude = 0;
    }

    public PositionData(float longitude, float latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) { this.longitude = longitude; }
    public float getLongitude() { return longitude; }
    public void setLatitude(float latitude) { this.latitude = latitude; }
    public float getLatitude() { return latitude; }
}

class PositionDataConverter {

    @TypeConverter
    public static PositionData toPositionData(String value) {
        Scanner sc = new Scanner(value);
        sc.useDelimiter("\t");
        float latitude = sc.nextFloat();
        float longitude = sc.nextFloat();
        return new PositionData(latitude, longitude);
    }

    @TypeConverter
    public static String toString(PositionData positionData) {
        return String.valueOf(positionData.getLatitude()) + "\t" + String.valueOf(positionData.getLongitude());
    }

}