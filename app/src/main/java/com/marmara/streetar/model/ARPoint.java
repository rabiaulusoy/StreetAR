package com.marmara.streetar.model;

import android.location.Location;

/**
 * Created by ntdat on 1/16/17.
 */

public class ARPoint {
    Location location;
    String name;
    double size;
    float x_start;
    float y_start;
    boolean visible;


    public ARPoint(String name, double lat, double lon, double altitude, double size, float x_start, float y_start, boolean visible) {
        this.location = new Location("ARPoint");
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(altitude);
        this.name = name;
        this.size = size;
        this.x_start = x_start;
        this.y_start = y_start;
        this.visible = visible;
    }

    public ARPoint(String name, double lat, double lon, double altitude) {
        this.name = name;
        location = new Location("ARPoint");
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(altitude);
        this.size = 0;
        this.x_start = 0;
        this.y_start = 0;
        this.visible = true;

    }

    public Location getLocation() {
    return location;
    }

    public String getName() {
        return name;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public float getX_start() {
        return x_start;
    }

    public void setX_start(float x_start) {
        this.x_start = x_start;
    }

    public float getY_start() {
        return y_start;
    }

    public void setY_start(float y_start) {
        this.y_start = y_start;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
