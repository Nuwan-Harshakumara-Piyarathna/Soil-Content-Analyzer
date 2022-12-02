package com.example.soilcontentanalyzer.Model;

public class Measurement {
    private int location;
    private double N;
    private double P;
    private double K;
    private double ph;
    private double latitude;
    private double longitude;

    public Measurement(int location, double n, double p, double k, double ph, double latitude, double longitude) {
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.N = n;
        this.P = p;
        this.K = k;
        this.ph = ph;
    }

    public String getLocation() {
        return "" + location;
    }

    public double getN() {
        return N;
    }

    public double getP() {
        return P;
    }

    public double getK() {
        return K;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getPh() {
        return ph;
    }

    public void setPh(double ph) {
        this.ph = ph;
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "location=" + location +
                ", N=" + N +
                ", P=" + P +
                ", K=" + K +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
