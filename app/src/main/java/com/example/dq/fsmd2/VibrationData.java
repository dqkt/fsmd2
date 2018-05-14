package com.example.dq.fsmd2;

import android.arch.persistence.room.TypeConverter;

import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Created by DQ on 12/16/2017.
 */

public class VibrationData {
    private float peakXVibration;
    private float peakYVibration;
    private float peakZVibration;

    private float avgXVibration;
    private float avgYVibration;
    private float avgZVibration;

    private float peakVibMagnitude;
    private float avgVibMagnitude;

    public VibrationData() {
        peakXVibration = 0;
        peakYVibration = 0;
        peakZVibration = 0;

        avgXVibration = 0;
        avgYVibration = 0;
        avgZVibration = 0;
    }

    public VibrationData(float peakXVib, float peakYVib, float peakZVib,
                         float avgXVib, float avgYVib, float avgZVib) {
        this.peakXVibration = peakXVib;
        this.peakYVibration = peakYVib;
        this.peakZVibration = peakZVib;

        this.peakVibMagnitude = calculateVibrationMagnitude(peakXVib, peakYVib, peakZVib);

        this.avgXVibration = avgXVib;
        this.avgYVibration = avgYVib;
        this.avgZVibration = avgZVib;

        this.avgVibMagnitude = calculateVibrationMagnitude(avgXVib, avgYVib, avgZVib);
    }

    public VibrationData(float peakXVib, float peakYVib, float peakZVib,
                         float avgXVib, float avgYVib, float avgZVib,
                         float peakVibMagnitude, float avgVibMagnitude) {
        this.peakXVibration = peakXVib;
        this.peakYVibration = peakYVib;
        this.peakZVibration = peakZVib;

        this.peakVibMagnitude = peakVibMagnitude;

        this.avgXVibration = avgXVib;
        this.avgYVibration = avgYVib;
        this.avgZVibration = avgZVib;

        this.avgVibMagnitude = avgVibMagnitude;
    }

    public void setPeakXVibration(float peakXVib) { this.peakXVibration = peakXVib; }
    public float getPeakXVibration() { return peakXVibration; }
    public void setPeakYVibration(float peakYVib) { this.peakYVibration = peakYVib; }
    public float getPeakYVibration() { return peakYVibration; }
    public void setPeakZVibration(float peakZVib) { this.peakZVibration = peakZVib; }
    public float getPeakZVibration() { return peakZVibration; }

    public void setAvgXVibration(float avgXVib) { this.avgXVibration = avgXVib; }
    public float getAvgXVibration() { return avgXVibration; }
    public void setAvgYVibration(float avgYVib) { this.avgYVibration = avgYVib; }
    public float getAvgYVibration() { return avgYVibration; }
    public void setAvgZVibration(float avgZVib) { this.avgZVibration = avgZVib; }
    public float getAvgZVibration() { return avgZVibration; }

    public float getPeakVibMagnitude() {
        return peakVibMagnitude;
    }
    public void setPeakVibMagnitude(float peakVibMagnitude) {
        this.peakVibMagnitude = peakVibMagnitude;
    }
    public float getAvgVibMagnitude() {
        return avgVibMagnitude;
    }
    public void setAvgVibMagnitude(float avgVibMagnitude) {
        this.avgVibMagnitude = avgVibMagnitude;
    }

    public static float calculateVibrationMagnitude(float xVibration, float yVibration, float zVibration) {
        return (float) Math.sqrt(Math.pow(xVibration, 2) + Math.pow(yVibration, 2) + Math.pow(zVibration, 2));
    }
}

class VibrationDataConverter {

    @TypeConverter
    public static VibrationData toVibrationData(String value) {
        Scanner sc = new Scanner(value);
        sc.useDelimiter("\t");
        return new VibrationData(sc.nextFloat(), sc.nextFloat(), sc.nextFloat(), sc.nextFloat(),
                sc.nextFloat(), sc.nextFloat(), sc.nextFloat(), sc.nextFloat());
    }

    @TypeConverter
    public static String toString(VibrationData vibrationData) {
        return String.valueOf(vibrationData.getPeakXVibration()) + "\t" + String.valueOf(vibrationData.getPeakYVibration()) + "\t" +
                String.valueOf(vibrationData.getPeakZVibration()) + "\t" + String.valueOf(vibrationData.getAvgXVibration()) + "\t" +
                String.valueOf(vibrationData.getAvgYVibration()) + "\t" + String.valueOf(vibrationData.getAvgZVibration()) + "\t" +
                String.valueOf(vibrationData.getPeakVibMagnitude()) + "\t" + String.valueOf(vibrationData.getAvgVibMagnitude());
    }

}