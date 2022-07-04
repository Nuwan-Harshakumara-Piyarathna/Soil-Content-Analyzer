package com.example.soilcontentanalyzer.Model;

import java.text.DecimalFormat;

public class Measurement {
    private int location;
    private String N;
    private String P;
    private String K;
    private double latitude;
    private double longitude;

    public Measurement(int location, double n, double p, double k, double latitude, double longitude) {
        this.location = location;
        DecimalFormat df = new DecimalFormat("#.0");
        N = df.format(n);
        P = df.format(p);
        K = df.format(k);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocation() {
        return "" + location;
    }

    public String getN() {
        return N;
    }

    public String getP() {
        return P;
    }

    public String getK() {
        return K;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
