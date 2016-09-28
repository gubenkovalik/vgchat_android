package ml.fastest.vgchat.gps.listeners;

/**
 * Created by Джен Кот on 28.09.2016.
 */

public interface GPSResponseListener {

    void onSuccess(String response);

    void onFailure(String response, int responseCode);
}
