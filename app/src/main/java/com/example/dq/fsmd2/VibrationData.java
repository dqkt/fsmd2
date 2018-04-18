package com.example.dq.fsmd2;

import android.arch.persistence.room.TypeConverter;

import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Created by DQ on 12/16/2017.
 */

public class VibrationData {
    private double peakXVibration;
    private double peakYVibration;
    private double peakZVibration;

    private double avgXVibration;
    private double avgYVibration;
    private double avgZVibration;

    private double peakVibMagnitude;
    private double avgVibMagnitude;

    public VibrationData() {
        peakXVibration = 0;
        peakYVibration = 0;
        peakZVibration = 0;

        avgXVibration = 0;
        avgYVibration = 0;
        avgZVibration = 0;
    }

    public VibrationData(double peakXVib, double peakYVib, double peakZVib,
                         double avgXVib, double avgYVib, double avgZVib) {
        this.peakXVibration = peakXVib;
        this.peakYVibration = peakYVib;
        this.peakZVibration = peakZVib;

        this.peakVibMagnitude = calculateVibrationMagnitude(peakXVib, peakYVib, peakZVib);

        this.avgXVibration = avgXVib;
        this.avgYVibration = avgYVib;
        this.avgZVibration = avgZVib;

        this.avgVibMagnitude = calculateVibrationMagnitude(avgXVib, avgYVib, avgZVib);
    }

    public VibrationData(double peakXVib, double peakYVib, double peakZVib,
                         double avgXVib, double avgYVib, double avgZVib,
                         double peakVibMagnitude, double avgVibMagnitude) {
        this.peakXVibration = peakXVib;
        this.peakYVibration = peakYVib;
        this.peakZVibration = peakZVib;

        this.peakVibMagnitude = peakVibMagnitude;

        this.avgXVibration = avgXVib;
        this.avgYVibration = avgYVib;
        this.avgZVibration = avgZVib;

        this.avgVibMagnitude = avgVibMagnitude;
    }

    public void setPeakXVibration(double peakXVib) { this.peakXVibration = peakXVib; }
    public double getPeakXVibration() { return peakXVibration; }
    public void setPeakYVibration(double peakYVib) { this.peakYVibration = peakYVib; }
    public double getPeakYVibration() { return peakYVibration; }
    public void setPeakZVibration(double peakZVib) { this.peakZVibration = peakZVib; }
    public double getPeakZVibration() { return peakZVibration; }

    public void setAvgXVibration(double avgXVib) { this.avgXVibration = avgXVib; }
    public double getAvgXVibration() { return avgXVibration; }
    public void setAvgYVibration(double avgYVib) { this.avgYVibration = avgYVib; }
    public double getAvgYVibration() { return avgYVibration; }
    public void setAvgZVibration(double avgZVib) { this.avgZVibration = avgZVib; }
    public double getAvgZVibration() { return avgZVibration; }

    public double getPeakVibMagnitude() {
        return peakVibMagnitude;
    }
    public void setPeakVibMagnitude(double peakVibMagnitude) {
        this.peakVibMagnitude = peakVibMagnitude;
    }
    public double getAvgVibMagnitude() {
        return avgVibMagnitude;
    }
    public void setAvgVibMagnitude(double avgVibMagnitude) {
        this.avgVibMagnitude = avgVibMagnitude;
    }

    public static double calculateVibrationMagnitude(double xVibration, double yVibration, double zVibration) {
        return Math.sqrt(Math.pow(xVibration, 2) + Math.pow(yVibration, 2) + Math.pow(zVibration, 2));
    }
}

class VibrationDataConverter {

    @TypeConverter
    public static VibrationData toVibrationData(String value) {
        Scanner sc = new Scanner(value);
        sc.useDelimiter("\t");
        return new VibrationData(sc.nextDouble(), sc.nextDouble(), sc.nextDouble(), sc.nextDouble(),
                sc.nextDouble(), sc.nextDouble(), sc.nextDouble(), sc.nextDouble());
    }

    @TypeConverter
    public static String toString(VibrationData vibrationData) {
        return String.valueOf(vibrationData.getPeakXVibration()) + "\t" + String.valueOf(vibrationData.getPeakYVibration()) + "\t" +
                String.valueOf(vibrationData.getPeakZVibration()) + "\t" + String.valueOf(vibrationData.getAvgXVibration()) + "\t" +
                String.valueOf(vibrationData.getAvgYVibration()) + "\t" + String.valueOf(vibrationData.getAvgZVibration()) + "\t" +
                String.valueOf(vibrationData.getPeakVibMagnitude()) + "\t" + String.valueOf(vibrationData.getAvgVibMagnitude());
    }

}