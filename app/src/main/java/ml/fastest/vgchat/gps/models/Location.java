package ml.fastest.vgchat.gps.models;

/**
 * Created by Джен Кот on 28.09.2016.
 */

public class Location {

    private double lat;

    private double lng;

    private double acc;

    private double alt;

    public Location(double lat, double lng, double acc, double alt) {
        this.lat = lat;
        this.lng = lng;
        this.acc = acc;
        this.alt = alt;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getAcc() {
        return acc;
    }

    public double getAlt() {
        return alt;
    }
}
