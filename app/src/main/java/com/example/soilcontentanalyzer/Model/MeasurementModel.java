package com.example.soilcontentanalyzer.Model;

import java.text.DecimalFormat;

public class MeasurementModel {
    private int location;
    private String N;
    private String P;
    private String K;

    public MeasurementModel(int location, double n, double p, double k) {
        this.location = location;
        DecimalFormat df = new DecimalFormat("#.0");
        N = df.format(n);
        P = df.format(p);
        K = df.format(k);
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
}
