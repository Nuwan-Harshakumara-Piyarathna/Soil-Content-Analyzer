package com.example.soilcontentanalyzer.Model;

import java.util.ArrayList;

public class FieldMap {
    private ArrayList<Path> paths;
    private ArrayList<Measurement> measurements;

    public FieldMap(ArrayList<Path> paths, ArrayList<Measurement> measurements) {
        this.paths = paths;
        this.measurements = measurements;
    }

    public ArrayList<Path> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<Path> paths) {
        this.paths = paths;
    }

    public ArrayList<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(ArrayList<Measurement> measurements) {
        this.measurements = measurements;
    }
}
