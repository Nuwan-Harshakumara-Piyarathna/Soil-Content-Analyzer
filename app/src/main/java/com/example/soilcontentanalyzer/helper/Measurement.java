package com.example.soilcontentanalyzer.helper;

public class Measurement {
    private double NPK[] = new double[3];
    private int locationNo;

    public Measurement(int locationNo, double n, double p, double k) {
        this.NPK[0] = n;
        this.NPK[1] = p;
        this.NPK[2] = k;
        this.locationNo = locationNo;
    }

    public double[] getNPK() {
        return NPK;
    }

    public int getLocationNo() {
        return locationNo;
    }
}
