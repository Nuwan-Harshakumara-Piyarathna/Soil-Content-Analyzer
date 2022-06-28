package com.example.soilcontentanalyzer.helper;

public class Path {
    private double latitudeP1;
    private double longitudeP1;
    private double latitudeP2;
    private double longitudeP2;

    public Path(double latitudeP1, double longitudeP1, double latitudeP2, double longitudeP2) {
        this.latitudeP1 = latitudeP1;
        this.longitudeP1 = longitudeP1;
        this.latitudeP2 = latitudeP2;
        this.longitudeP2 = longitudeP2;
    }

    public double getLatitudeP1() {
        return latitudeP1;
    }

    public double getLongitudeP1() {
        return longitudeP1;
    }

    public double getLatitudeP2() {
        return latitudeP2;
    }

    public double getLongitudeP2() {
        return longitudeP2;
    }
}
