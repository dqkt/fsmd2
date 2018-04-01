package com.example.dq.fsmd2;

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

        this.avgXVibration = avgXVib;
        this.avgYVibration = avgYVib;
        this.avgZVibration = avgZVib;
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
}
