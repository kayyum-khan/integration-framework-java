package com.appearnetworks.aiq.integrationframework.context;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Geographic location of a mobile device.
 */
public class Location {
    private static final String LOCATION_KEY = "com.appearnetworks.aiq.location";
    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";

    private double latitude;
    private double longitude;

    /**
     * Needed for Jackson deserialization, do not use.
     */
    public Location() { }

    /**
     * Main constructor.
     */
    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Get location from client context.
     *
     * @param clientContext client context (may be null)
     * @return location, or {@code null} if context is {@code null} or doesn't contain location
     */
    public static Location fromContext(JsonNode clientContext) {
        if (clientContext == null) return null;
        if (!clientContext.has(LOCATION_KEY)) return null;

        if (!(clientContext.get(LOCATION_KEY).has(LATITUDE_KEY) && clientContext.get(LOCATION_KEY).has(LONGITUDE_KEY)))
            return null;

        double latitude = clientContext.get(LOCATION_KEY).get(LATITUDE_KEY).doubleValue();
        double longitude = clientContext.get(LOCATION_KEY).get(LONGITUDE_KEY).doubleValue();

        return new Location(latitude, longitude);
    }

    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
