package com.myk.vance_gimbal_submission.domain;

public class GeofenceDomainModel {
    private final Double latitude;
    private final Double longitude;
    private final Float radius;

    public GeofenceDomainModel(Double latitude, Double longitude, Float radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Float getRadius() {
        return radius;
    }

    public String stringify() {
        return String.format("latitude: %s, longitude: %s, radius: %s", latitude, longitude, radius);
    }
}
