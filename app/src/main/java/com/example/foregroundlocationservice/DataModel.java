package com.example.foregroundlocationservice;

public class DataModel {
    private String latitude, longitude;

    public DataModel(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLati() {
        return latitude;
    }

    public void setLati(String latitude) {
        this.latitude = latitude;
    }

    public String getLongi() {
        return longitude;
    }

    public void setLongi(String longitude) {
        this.longitude = longitude;
    }
}
