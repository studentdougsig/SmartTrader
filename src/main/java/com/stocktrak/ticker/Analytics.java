package com.stocktrak.ticker;

/**
 * Created by Douglas on 2/16/2015.
 */
public class Analytics {
    private double average;
    private double standardDeviation;

    public Analytics(double average, double standardDeviation) {
        this.average = average;
        this.standardDeviation = standardDeviation;
    }

    public double getAverage() {
        return average;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    @Override
    public String toString() {
        return "Analytics{" +
                "average=" + average +
                ", standardDeviation=" + standardDeviation +
                '}';
    }
}
