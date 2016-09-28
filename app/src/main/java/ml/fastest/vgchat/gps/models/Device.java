package ml.fastest.vgchat.gps.models;

/**
 * Created by Джен Кот on 28.09.2016.
 */

public class Device {

    private String imei;

    private String name;

    public Device(String imei, String name) {
        this.imei = imei;
        this.name = name;
    }

    public String getImei() {
        return imei;
    }

    public String getName() {
        return name;
    }
}
